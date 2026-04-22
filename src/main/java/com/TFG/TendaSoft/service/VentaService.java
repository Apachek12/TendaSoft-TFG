package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.dto.EstadisticasDTO;
import com.TFG.TendaSoft.dto.LineaVentaDTO;
import com.TFG.TendaSoft.dto.VentaListadoDTO;
import com.TFG.TendaSoft.model.*;
import com.TFG.TendaSoft.repository.*;
import com.TFG.TendaSoft.utils.VerifactuUtils;
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

    @Transactional
    public Venta registrarNuevaVenta(Venta venta, List<LineaVenta> lineas) {
        if (venta == null) throw new RuntimeException("El objeto venta es nulo.");

        DatosNegocio negocio = datosNegocioRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Error: Configure los datos de la empresa en el panel de administración."));

        venta.setNumeroFactura(generarSiguienteNumeroFactura());
        venta.setFecha(LocalDateTime.now());
        if (venta.getTipoFactura() == null) venta.setTipoFactura("F2");

        Optional<Venta> ultimaVentaOpt = ventaRepository.findFirstByOrderByIdDesc();
        String hashAnterior = ultimaVentaOpt.map(Venta::getHashVerifactu).orElse("INICIO-SISTEMA");
        venta.setHashAnterior(hashAnterior);

        BigDecimal baseImponibleTotal = BigDecimal.ZERO;
        BigDecimal cuotaIvaTotal = BigDecimal.ZERO;

        if (venta.getLineas() == null) venta.setLineas(new ArrayList<>());

        for (LineaVenta linea : lineas) {
            Producto producto = productoRepository.findById(linea.getProducto().getCodigoBarras())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + linea.getProducto().getCodigoBarras()));

            if (producto.getUnidades() < linea.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente de: " + producto.getNombre());
            }

            producto.setUnidades(producto.getUnidades() - linea.getCantidad());
            productoRepository.save(producto);

            BigDecimal cantidadBD = new BigDecimal(linea.getCantidad());
            BigDecimal totalLineaPVP = producto.getPrecio().multiply(cantidadBD);
            BigDecimal porcIva = producto.getPorcentajeIva() != null ? producto.getPorcentajeIva() : BigDecimal.ZERO;

            BigDecimal divisor = BigDecimal.ONE.add(porcIva.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            BigDecimal baseLinea = totalLineaPVP.divide(divisor, 2, RoundingMode.HALF_UP);
            BigDecimal ivaLinea = totalLineaPVP.subtract(baseLinea);

            BigDecimal precioUnitarioSinIva = baseLinea.divide(cantidadBD, 2, RoundingMode.HALF_UP);

            linea.setPrecioUnitario(precioUnitarioSinIva);
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

        String hashGenerado = VerifactuUtils.generarHashVerifactu(
                negocio.getCif(),
                venta.getNumeroFactura(),
                venta.getFecha(),
                venta.getTipoFactura(),
                venta.getCuotaIvaTotal(),
                venta.getTotal(),
                venta.getHashAnterior()
        );
        venta.setHashVerifactu(hashGenerado);
        venta.setEstadoVerifactu("PENDIENTE_ENVIO");

        Venta ventaGuardada = ventaRepository.save(venta);
        for (LineaVenta l : lineas) {
            l.setVenta(ventaGuardada);
            lineaVentaRepository.save(l);
        }

        // --- BLOQUE VERIFACTU: GENERACIÓN, VALIDACIÓN Y FIRMA ---
        try {
            // 1. Generar el XML
            String xmlFactura = xmlService.generarXmlAltaFactura(ventaGuardada, lineas, negocio);

            // 2. VALIDAR EL XML (¡Añadimos esta línea!)
            boolean esValido = xmlService.validarXmlContraEsquema(xmlFactura);
            if (!esValido) {
                // Al lanzar RuntimeException, el @Transactional hará Rollback (borra la venta y devuelve stock)
                throw new RuntimeException("ERROR AEAT: El XML generado no es válido según el esquema oficial.");
            }

            // 3. Firmar el XML
            String rutaCert = negocio.getRutaCertificado();
            String passCert = "123456";

            if (rutaCert != null && !rutaCert.isEmpty()) {
                String xmlFirmado = firmaDigitalService.firmarXml(xmlFactura, rutaCert, passCert);
                ventaGuardada.setEstadoVerifactu("FIRMADO_Y_PENDIENTE_ENVIO");
                ventaRepository.save(ventaGuardada);
                System.out.println("✅ XML Validado y Firmado con éxito.");
            }
        } catch (Exception e) {
            // Si el error es de validación, relanzamos la excepción para anular la venta
            if (e.getMessage().contains("ERROR AEAT")) {
                throw e;
            }
            System.err.println("Error en proceso VeriFactu (Firma): " + e.getMessage());
        }

        return ventaGuardada;
    }

    private String generarSiguienteNumeroFactura() {
        int anioActual = LocalDate.now().getYear();
        String prefijo = "FAC-" + anioActual;
        Optional<Venta> ultimaVentaOpt = ventaRepository.findFirstByNumeroFacturaStartingWithOrderByIdDesc(prefijo);

        if (ultimaVentaOpt.isPresent()) {
            try {
                String ultimoNumStr = ultimaVentaOpt.get().getNumeroFactura();
                String[] partes = ultimoNumStr.split("-");
                int ultimoSecuencial = Integer.parseInt(partes[2]);
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