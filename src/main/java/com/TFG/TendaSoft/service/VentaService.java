package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.dto.EstadisticasDTO;
import com.TFG.TendaSoft.dto.LineaVentaDTO;
import com.TFG.TendaSoft.dto.VentaListadoDTO;
import com.TFG.TendaSoft.model.*;
import com.TFG.TendaSoft.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final LineaVentaRepository lineaVentaRepository;
    private final ProductoRepository productoRepository;
    private final DatosNegocioRepository datosNegocioRepository;
    private final VerifactuXmlService xmlService;
    private final FirmaDigitalService firmaDigitalService;
    private final VerifactuHttpService verifactuHttpService;

    @Transactional
    public Venta registrarNuevaVenta(Venta venta, List<LineaVenta> lineas) {
        if (venta == null) throw new RuntimeException("El objeto venta es nulo.");

        DatosNegocio negocio = datosNegocioRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Error: Configure los datos de la empresa."));

        // 1. Asignar número y fecha
        venta.setNumeroFactura(generarSiguienteNumeroFactura());
        venta.setFecha(LocalDateTime.now());
        if (venta.getTipoFactura() == null) venta.setTipoFactura("F2");

        // 2. Encadenar solo a la última factura CORRECTAMENTE aceptada por la AEAT
        Optional<Venta> ultimaVentaOpt = ventaRepository
                .findFirstByEstadoVerifactuOrderByIdDesc("CORRECTO");
        if (ultimaVentaOpt.isPresent()) {
            Venta anterior = ultimaVentaOpt.get();
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

        // --- CÁLCULOS DE LÍNEAS E IVA ---
        BigDecimal baseImponibleTotal = BigDecimal.ZERO;
        BigDecimal cuotaIvaTotal = BigDecimal.ZERO;
        if (venta.getLineas() == null) venta.setLineas(new ArrayList<>());

        for (LineaVenta linea : lineas) {
            Producto producto = productoRepository.findById(linea.getProducto().getCodigoBarras())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado."));

            producto.setUnidades(producto.getUnidades() - linea.getCantidad());
            productoRepository.save(producto);

            BigDecimal cantidadBD = new BigDecimal(linea.getCantidad());
            BigDecimal totalLineaPVP = producto.getPrecio().multiply(cantidadBD);
            BigDecimal porcIva = producto.getPorcentajeIva() != null ? producto.getPorcentajeIva() : BigDecimal.ZERO;

            BigDecimal divisor = BigDecimal.ONE.add(porcIva.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            BigDecimal baseLinea = totalLineaPVP.divide(divisor, 2, RoundingMode.HALF_UP);
            BigDecimal ivaLinea = totalLineaPVP.subtract(baseLinea);

            linea.setPrecioUnitario(baseLinea.divide(cantidadBD, 2, RoundingMode.HALF_UP));
            linea.setPorcentajeIva(porcIva);
            linea.setImporteIva(ivaLinea);
            linea.setNombreProducto(producto.getNombre());
            linea.setVenta(venta);

            baseImponibleTotal = baseImponibleTotal.add(baseLinea);
            cuotaIvaTotal = cuotaIvaTotal.add(ivaLinea);
        }

        venta.setBaseImponibleTotal(baseImponibleTotal);
        venta.setCuotaIvaTotal(cuotaIvaTotal);
        venta.setTotal(baseImponibleTotal.add(cuotaIvaTotal));

        // 3. Guardado inicial
        Venta ventaGuardada = ventaRepository.save(venta);
        for (LineaVenta l : lineas) {
            l.setVenta(ventaGuardada);
            lineaVentaRepository.save(l);
        }

        // --- BLOQUE VERIFACTU ---
        try {
            System.out.println(">>> 1. Iniciando proceso VeriFactu...");
            String xmlFactura = xmlService.generarXmlAltaFactura(ventaGuardada, lineas, negocio);
            System.out.println(">>> 2. XML Generado. Longitud: " + xmlFactura.length());

            if (xmlService.validarXmlContraEsquema(xmlFactura)) {
                String rutaCert = negocio.getRutaCertificado();
                String passCert = negocio.getCertificadoPassword();

                if (rutaCert != null && passCert != null) {
                    System.out.println(">>> 3. Firmando XML...");
                    String xmlFirmado = firmaDigitalService.firmarXml(xmlFactura, rutaCert, passCert);
                    ventaGuardada.setXmlFirmado(xmlFirmado);
                    System.out.println(">>> 4. XML Firmado correctamente.");

                    try {
                        System.out.println(">>> 5. Enviando a la AEAT...");
                        String respuestaAeat = verifactuHttpService.enviarFacturaAEAT(xmlFirmado, rutaCert, passCert);
                        System.out.println(">>> 6. Respuesta AEAT: " + respuestaAeat);

                        if (esRespuestaCorrecta(respuestaAeat)) {
                            ventaGuardada.setEstadoVerifactu("CORRECTO");
                        } else {
                            ventaGuardada.setEstadoVerifactu("ERROR_AEAT");
                        }
                    } catch (Exception e) {
                        ventaGuardada.setEstadoVerifactu("FIRMADO_Y_PENDIENTE_ENVIO");
                        System.err.println(">>> ⚠️ Error HTTP con AEAT: " + e.getMessage());
                    }

                    ventaGuardada = ventaRepository.saveAndFlush(ventaGuardada);
                    System.out.println(">>> 7. Estado final guardado: " + ventaGuardada.getEstadoVerifactu());
                } else {
                    System.err.println(">>> ❌ Faltan credenciales de certificado.");
                }
            } else {
                System.err.println(">>> ❌ XML falló la validación.");
            }
        } catch (Exception e) {
            System.err.println(">>> 🚨 Error CRÍTICO VeriFactu: " + e.getMessage());
            e.printStackTrace();
            ventaGuardada.setEstadoVerifactu("ERROR_FIRMA");
            ventaRepository.save(ventaGuardada);
        }

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
            venta.setEstadoVerifactu("ERROR_FIRMA");
            return ventaRepository.save(venta);
        }

        try {
            String xmlFirmado = venta.getXmlFirmado();
            boolean tieneFirmado = xmlFirmado != null && !xmlFirmado.isBlank();

            if (!tieneFirmado) {
                System.out.println(">>> Reintento: regenerando y firmando XML...");
                String xmlFactura = xmlService.generarXmlAltaFactura(venta, venta.getLineas(), negocio);
                if (!xmlService.validarXmlContraEsquema(xmlFactura)) {
                    venta.setEstadoVerifactu("ERROR_XML");
                    return ventaRepository.save(venta);
                }
                xmlFirmado = firmaDigitalService.firmarXml(xmlFactura, rutaCert, passCert);
                venta.setXmlFirmado(xmlFirmado);
            } else {
                System.out.println(">>> Reintento: reutilizando XML firmado existente...");
            }

            venta.setEstadoVerifactu("FIRMADO");

            try {
                String respuestaAeat = verifactuHttpService.enviarFacturaAEAT(xmlFirmado, rutaCert, passCert);
                if (esRespuestaCorrecta(respuestaAeat)) {
                    venta.setEstadoVerifactu("CORRECTO");
                } else {
                    venta.setEstadoVerifactu("ERROR_AEAT");
                    System.err.println(">>> Reintento: respuesta AEAT incorrecta: " + respuestaAeat);
                }
            } catch (Exception e) {
                venta.setEstadoVerifactu("FIRMADO_Y_PENDIENTE_ENVIO");
                System.err.println(">>> Reintento: error HTTP: " + e.getMessage());
            }

        } catch (Exception e) {
            venta.setEstadoVerifactu("ERROR_FIRMA");
            System.err.println(">>> Reintento: error al firmar: " + e.getMessage());
        }

        return ventaRepository.save(venta);
    }

    /**
     * La AEAT usa namespaces (tikR:EstadoEnvio, tikR:EstadoRegistro).
     * Buscar ">Correcto<" funciona independientemente del namespace.
     */
    private boolean esRespuestaCorrecta(String respuesta) {
        return respuesta.contains(">Correcto<");
    }

    private String generarSiguienteNumeroFactura() {
        int anioActual = LocalDate.now().getYear();
        String prefijo = "FAC-" + anioActual;
        Optional<Venta> ultimaVentaOpt = ventaRepository
                .findFirstByNumeroFacturaStartingWithOrderByIdDesc(prefijo);

        if (ultimaVentaOpt.isPresent()) {
            try {
                String ultimoNumStr = ultimaVentaOpt.get().getNumeroFactura();
                String[] partes = ultimoNumStr.split("-");
                int ultimoSecuencial = Integer.parseInt(partes[partes.length - 1]);
                return String.format("FAC-%d-%04d", anioActual, ultimoSecuencial + 1);
            } catch (Exception e) {
                return String.format("FAC-%d-0001", anioActual);
            }
        }
        return String.format("FAC-%d-0001", anioActual);
    }

    public List<VentaListadoDTO> obtenerVentasPorPeriodo(Integer año, Integer mes, Integer dia) {
        LocalDateTime inicio;
        LocalDateTime fin;

        if (mes == null) {
            inicio = LocalDateTime.of(año, 1, 1, 0, 0, 0);
            fin = LocalDateTime.of(año, 12, 31, 23, 59, 59);
        } else if (dia == null) {
            YearMonth yearMonth = YearMonth.of(año, mes);
            inicio = yearMonth.atDay(1).atStartOfDay();
            fin = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        } else {
            inicio = LocalDateTime.of(año, mes, dia, 0, 0, 0);
            fin = LocalDateTime.of(año, mes, dia, 23, 59, 59);
        }

        List<Venta> ventasEntidad = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
        return ventasEntidad.stream().map(this::convertirADTO).toList();
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

    public EstadisticasDTO obtenerEstadisticasDePeriodo(Integer año, Integer mes, Integer dia) {
        List<VentaListadoDTO> ventas = obtenerVentasPorPeriodo(año, mes, dia);

        if (ventas.isEmpty()) {
            return EstadisticasDTO.builder()
                    .totalFacturado(BigDecimal.ZERO)
                    .totalTickets(0L)
                    .ticketMedio(BigDecimal.ZERO)
                    .ventasPorMetodoPago(new java.util.HashMap<>())
                    .productosVendidos(0L)
                    .build();
        }

        BigDecimal totalFacturado = ventas.stream().map(VentaListadoDTO::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Long totalTickets = (long) ventas.size();
        BigDecimal ticketMedio = totalFacturado.divide(new BigDecimal(totalTickets), 2, RoundingMode.HALF_UP);

        java.util.Map<String, BigDecimal> ventasPorMetodo = ventas.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        VentaListadoDTO::getMetodoPago,
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO, VentaListadoDTO::getTotal, BigDecimal::add)
                ));

        return EstadisticasDTO.builder()
                .totalFacturado(totalFacturado)
                .totalTickets(totalTickets)
                .ticketMedio(ticketMedio)
                .ventasPorMetodoPago(ventasPorMetodo)
                .productosVendidos(0L)
                .build();
    }
}

/*TODO Cuándo se enciende/apaga el TPV.
Cuándo hay un error en la firma (lo que ya tienes en el catch).
Intentos de acceso no autorizados. */