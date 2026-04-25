package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.dto.EstadisticasDTO;
import com.TFG.TendaSoft.dto.LineaVentaDTO;
import com.TFG.TendaSoft.dto.VentaListadoDTO;
import com.TFG.TendaSoft.model.*;
import com.TFG.TendaSoft.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository          ventaRepository;
    private final LineaVentaRepository     lineaVentaRepository;
    private final ProductoRepository       productoRepository;
    private final DatosNegocioRepository   datosNegocioRepository;
    private final VerifactuXmlService      xmlService;
    private final FirmaDigitalService      firmaDigitalService;
    private final VerifactuHttpService     verifactuHttpService;

    @Transactional
    public Venta registrarNuevaVenta(Venta venta, List<LineaVenta> lineas) {
        if (venta == null) throw new RuntimeException("El objeto de venta es nulo.");

        DatosNegocio negocio = datosNegocioRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Configure los datos de la empresa antes de vender."));

        // 1. Número de factura y fecha
        venta.setNumeroFactura(generarSiguienteNumeroFactura());
        venta.setFecha(LocalDateTime.now());
        if (venta.getTipoFactura() == null) venta.setTipoFactura("F2");

        // 2. Encadenamiento VeriFactu: enlazamos a la última factura aceptada por la AEAT
        Optional<Venta> ultimaCorrecta = ventaRepository.findFirstByEstadoVerifactuOrderByIdDesc("CORRECTO");
        if (ultimaCorrecta.isPresent()) {
            Venta anterior = ultimaCorrecta.get();
            venta.setHashAnterior(anterior.getHashVerifactu());
            venta.setNumeroFacturaAnterior(anterior.getNumeroFactura());
            venta.setFechaAnterior(anterior.getFecha());
            venta.setCifEmisorAnterior(negocio.getCif());
        } else {
            venta.setHashAnterior("INICIO-SISTEMA");
            venta.setNumeroFacturaAnterior(null);
            venta.setFechaAnterior(null);
            venta.setCifEmisorAnterior(null);
        }

        venta.setHashVerifactu("PENDIENTE_CALCULO");
        venta.setEstadoVerifactu("PROCESANDO");

        // 3. Cálculo de IVA y base imponible por línea
        BigDecimal baseImponibleTotal = BigDecimal.ZERO;
        BigDecimal cuotaIvaTotal      = BigDecimal.ZERO;
        if (venta.getLineas() == null) venta.setLineas(new ArrayList<>());

        for (LineaVenta linea : lineas) {
            Producto producto = productoRepository.findById(linea.getProducto().getCodigoBarras())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado."));

            producto.setUnidades(producto.getUnidades() - linea.getCantidad());
            productoRepository.save(producto);

            BigDecimal cantidad = new BigDecimal(linea.getCantidad());
            BigDecimal totalPVP = producto.getPrecio().multiply(cantidad);
            BigDecimal porcIva  = producto.getPorcentajeIva() != null ? producto.getPorcentajeIva() : BigDecimal.ZERO;
            BigDecimal divisor  = BigDecimal.ONE.add(porcIva.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            BigDecimal base     = totalPVP.divide(divisor, 2, RoundingMode.HALF_UP);
            BigDecimal iva      = totalPVP.subtract(base);

            linea.setPrecioUnitario(base.divide(cantidad, 2, RoundingMode.HALF_UP));
            linea.setPorcentajeIva(porcIva);
            linea.setImporteIva(iva);
            linea.setNombreProducto(producto.getNombre());
            linea.setVenta(venta);

            baseImponibleTotal = baseImponibleTotal.add(base);
            cuotaIvaTotal      = cuotaIvaTotal.add(iva);
        }

        venta.setBaseImponibleTotal(baseImponibleTotal);
        venta.setCuotaIvaTotal(cuotaIvaTotal);
        venta.setTotal(baseImponibleTotal.add(cuotaIvaTotal));

        // 4. Guardado inicial en BD
        Venta ventaGuardada = ventaRepository.save(venta);
        for (LineaVenta l : lineas) {
            l.setVenta(ventaGuardada);
            lineaVentaRepository.save(l);
        }

        // 5. Proceso VeriFactu
        procesarVerifactu(ventaGuardada, lineas, negocio);
        return ventaGuardada;
    }

    @Transactional
    public Venta reintentarTramiteVerifactu(Long idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        DatosNegocio negocio = datosNegocioRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Configure los datos de la empresa"));

        String rutaCert = negocio.getRutaCertificado();
        String passCert = negocio.getCertificadoPassword();

        if (rutaCert == null || rutaCert.isEmpty() || passCert == null) {
            log.error("Reintento VeriFactu — faltan credenciales del certificado para factura {}", venta.getNumeroFactura());
            venta.setEstadoVerifactu("ERROR_FIRMA");
            return ventaRepository.save(venta);
        }

        try {
            String xmlFirmado = venta.getXmlFirmado();
            if (xmlFirmado == null || xmlFirmado.isBlank()) {
                log.info("Reintento VeriFactu — regenerando y firmando XML para factura {}", venta.getNumeroFactura());
                String xmlFactura = xmlService.generarXmlAltaFactura(venta, venta.getLineas(), negocio);
                xmlFirmado = firmaDigitalService.firmarXml(xmlFactura, rutaCert, passCert);
                venta.setXmlFirmado(xmlFirmado);
            } else {
                log.info("Reintento VeriFactu — reutilizando XML firmado para factura {}", venta.getNumeroFactura());
            }

            venta.setEstadoVerifactu("FIRMADO");

            try {
                String respuesta = verifactuHttpService.enviarFacturaAEAT(xmlFirmado, rutaCert, passCert);
                venta.setEstadoVerifactu(esRespuestaCorrecta(respuesta) ? "CORRECTO" : "ERROR_AEAT");
                if (!esRespuestaCorrecta(respuesta)) {
                    log.warn("Reintento VeriFactu — respuesta incorrecta de la AEAT para factura {}", venta.getNumeroFactura());
                }
            } catch (Exception e) {
                venta.setEstadoVerifactu("FIRMADO_Y_PENDIENTE_ENVIO");
                log.warn("Reintento VeriFactu — error HTTP con la AEAT: {}", e.getMessage());
            }

        } catch (Exception e) {
            venta.setEstadoVerifactu("ERROR_FIRMA");
            log.error("Reintento VeriFactu — error al firmar factura {}: {}", venta.getNumeroFactura(), e.getMessage());
        }

        return ventaRepository.save(venta);
    }

    public List<VentaListadoDTO> obtenerVentasPorPeriodo(Integer año, Integer mes, Integer dia) {
        LocalDateTime inicio;
        LocalDateTime fin;

        if (mes == null) {
            inicio = LocalDateTime.of(año, 1, 1, 0, 0, 0);
            fin    = LocalDateTime.of(año, 12, 31, 23, 59, 59);
        } else if (dia == null) {
            YearMonth ym = YearMonth.of(año, mes);
            inicio = ym.atDay(1).atStartOfDay();
            fin    = ym.atEndOfMonth().atTime(23, 59, 59);
        } else {
            inicio = LocalDateTime.of(año, mes, dia, 0, 0, 0);
            fin    = LocalDateTime.of(año, mes, dia, 23, 59, 59);
        }

        return ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin)
                .stream().map(this::convertirADTO).toList();
    }

    public EstadisticasDTO obtenerEstadisticasDePeriodo(Integer año, Integer mes, Integer dia) {
        List<VentaListadoDTO> ventas = obtenerVentasPorPeriodo(año, mes, dia);

        if (ventas.isEmpty()) {
            return EstadisticasDTO.builder()
                    .totalFacturado(BigDecimal.ZERO)
                    .totalTickets(0L)
                    .ticketMedio(BigDecimal.ZERO)
                    .ventasPorMetodoPago(new HashMap<>())
                    .productosVendidos(0L)
                    .build();
        }

        BigDecimal totalFacturado = ventas.stream()
                .map(VentaListadoDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Long totalTickets = (long) ventas.size();
        BigDecimal ticketMedio = totalFacturado.divide(new BigDecimal(totalTickets), 2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> ventasPorMetodo = ventas.stream()
                .collect(Collectors.groupingBy(
                        VentaListadoDTO::getMetodoPago,
                        Collectors.reducing(BigDecimal.ZERO, VentaListadoDTO::getTotal, BigDecimal::add)
                ));

        return EstadisticasDTO.builder()
                .totalFacturado(totalFacturado)
                .totalTickets(totalTickets)
                .ticketMedio(ticketMedio)
                .ventasPorMetodoPago(ventasPorMetodo)
                .productosVendidos(0L)
                .build();
    }

    // ── Métodos privados ─────────────────────────────────────────────────────

    private void procesarVerifactu(Venta ventaGuardada, List<LineaVenta> lineas, DatosNegocio negocio) {
        try {
            log.info("VeriFactu — generando XML para factura {}", ventaGuardada.getNumeroFactura());
            String xmlFactura = xmlService.generarXmlAltaFactura(ventaGuardada, lineas, negocio);

            String rutaCert = negocio.getRutaCertificado();
            String passCert = negocio.getCertificadoPassword();

            if (rutaCert == null || passCert == null) {
                log.error("VeriFactu — faltan credenciales del certificado");
                ventaGuardada.setEstadoVerifactu("ERROR_FIRMA");
                ventaRepository.save(ventaGuardada);
                return;
            }

            log.info("VeriFactu — firmando XML de factura {}", ventaGuardada.getNumeroFactura());
            String xmlFirmado = firmaDigitalService.firmarXml(xmlFactura, rutaCert, passCert);
            ventaGuardada.setXmlFirmado(xmlFirmado);

            try {
                log.info("VeriFactu — enviando factura {} a la AEAT", ventaGuardada.getNumeroFactura());
                String respuesta = verifactuHttpService.enviarFacturaAEAT(xmlFirmado, rutaCert, passCert);
                ventaGuardada.setEstadoVerifactu(esRespuestaCorrecta(respuesta) ? "CORRECTO" : "ERROR_AEAT");
                log.info("VeriFactu — estado final factura {}: {}", ventaGuardada.getNumeroFactura(), ventaGuardada.getEstadoVerifactu());
            } catch (Exception e) {
                ventaGuardada.setEstadoVerifactu("FIRMADO_Y_PENDIENTE_ENVIO");
                log.warn("VeriFactu — error HTTP con la AEAT, factura {} pendiente de envío: {}", ventaGuardada.getNumeroFactura(), e.getMessage());
            }

            ventaRepository.saveAndFlush(ventaGuardada);

        } catch (Exception e) {
            log.error("VeriFactu — error crítico procesando factura {}: {}", ventaGuardada.getNumeroFactura(), e.getMessage(), e);
            ventaGuardada.setEstadoVerifactu("ERROR_FIRMA");
            ventaRepository.save(ventaGuardada);
        }
    }

    // La AEAT usa namespaces en las etiquetas (tikR:EstadoEnvio, tikR:EstadoRegistro),
    // buscar ">Correcto<" funciona independientemente del namespace.
    private boolean esRespuestaCorrecta(String respuesta) {
        return respuesta.contains(">Correcto<");
    }

    private String generarSiguienteNumeroFactura() {
        int año = LocalDate.now().getYear();
        String prefijo = "FAC-" + año;
        return ventaRepository.findFirstByNumeroFacturaStartingWithOrderByIdDesc(prefijo)
                .map(ultima -> {
                    try {
                        String[] partes = ultima.getNumeroFactura().split("-");
                        int secuencial = Integer.parseInt(partes[partes.length - 1]);
                        return String.format("FAC-%d-%04d", año, secuencial + 1);
                    } catch (Exception e) {
                        return String.format("FAC-%d-0001", año);
                    }
                })
                .orElse(String.format("FAC-%d-0001", año));
    }

    private VentaListadoDTO convertirADTO(Venta venta) {
        return VentaListadoDTO.builder()
                .idVenta(venta.getId())
                .numeroFactura(venta.getNumeroFactura())
                .fecha(venta.getFecha())
                .total(venta.getTotal())
                .metodoPago(venta.getMetodoPago())
                .estadoVerifactu(venta.getEstadoVerifactu())
                .nombreCajero(venta.getUsuario() != null ? venta.getUsuario().getNombreUsuario() : "Desconocido")
                .lineas(venta.getLineas().stream()
                        .map(l -> LineaVentaDTO.builder()
                                .nombreProducto(l.getNombreProducto())
                                .cantidad(l.getCantidad())
                                .precioUnitario(l.getPrecioUnitario())
                                .build())
                        .toList())
                .build();
    }
}