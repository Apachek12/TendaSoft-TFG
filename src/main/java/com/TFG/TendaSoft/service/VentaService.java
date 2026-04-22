package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.dto.EstadisticasDTO;
import com.TFG.TendaSoft.dto.LineaVentaDTO;
import com.TFG.TendaSoft.dto.VentaListadoDTO;
import com.TFG.TendaSoft.model.DatosNegocio;
import com.TFG.TendaSoft.model.LineaVenta;
import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.model.Venta;
import com.TFG.TendaSoft.repository.DatosNegocioRepository;
import com.TFG.TendaSoft.repository.LineaVentaRepository;
import com.TFG.TendaSoft.repository.ProductoRepository;
import com.TFG.TendaSoft.repository.VentaRepository;
import com.TFG.TendaSoft.utils.VerifactuUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
        // 1. Obtener NIF del negocio (Requisito para el Hash y el XML)
        DatosNegocio negocio = datosNegocioRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Error: El administrador aún no ha configurado los datos de la empresa."));
        // 2. Generar Número de Factura
        venta.setNumeroFactura(generarSiguienteNumeroFactura());
        venta.setFecha(LocalDateTime.now());

        // 3. Obtener el Hash Anterior
        Optional<Venta> ultimaVentaOpt = ventaRepository.findFirstByOrderByIdDesc();
        String hashAnterior = ultimaVentaOpt.map(Venta::getHashVerifactu).orElse("INICIO-SISTEMA");
        venta.setHashAnterior(hashAnterior);

        // 4. Lógica de Líneas, Stock y cálculo de Totales (VERSIÓN PRECIO FINAL / PVP)
        BigDecimal baseImponibleTotal = BigDecimal.ZERO;
        BigDecimal cuotaIvaTotal = BigDecimal.ZERO;

        for (LineaVenta linea : lineas) {
            Producto producto = productoRepository.findById(linea.getProducto().getCodigoBarras())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getUnidades() < linea.getCantidad()) {
                throw new IllegalStateException("No hay stock suficiente de: " + producto.getNombre());
            }

            // Actualizar stock
            producto.setUnidades(producto.getUnidades() - linea.getCantidad());
            productoRepository.save(producto);

            // Cálculos económicos de la línea partiendo de un Precio FINAL (PVP)
            BigDecimal cantidadBD = new BigDecimal(linea.getCantidad());
            // El precio unitario que viene de la vista (o del producto) ya tiene el IVA
            BigDecimal totalLineaPVP = producto.getPrecio().multiply(cantidadBD);
            // Obtener el % de IVA del producto (Ej: 21.00)
            BigDecimal porcIva = producto.getPorcentajeIva() != null ? producto.getPorcentajeIva() : BigDecimal.ZERO;

            // Calculamos el divisor. Ej: para 21% será 1.21 (1 + 21/100)
            BigDecimal factorIva = BigDecimal.ONE.add(porcIva.divide(new BigDecimal("100")));

            // Extraemos la Base Imponible: Total PVP / 1.21 (Redondeando a 2 decimales para evitar errores)
            BigDecimal baseLinea = totalLineaPVP.divide(factorIva, 2, java.math.RoundingMode.HALF_UP);

            // El importe del IVA es simplemente lo que sobra
            BigDecimal ivaLinea = totalLineaPVP.subtract(baseLinea);

            // ¡IMPORTANTE PARA HACIENDA! La línea de venta se guarda con precios SIN IVA
            // Calculamos cuánto vale 1 sola unidad sin IVA para registrarlo correctamente
            BigDecimal precioUnitarioSinIva = baseLinea.divide(cantidadBD, 2, java.math.RoundingMode.HALF_UP);

            linea.setPrecioUnitario(precioUnitarioSinIva);
            linea.setPorcentajeIva(porcIva);
            linea.setImporteIva(ivaLinea);
            linea.setNombreProducto(producto.getNombre());

            // Sumar a los totales de la venta (Base real y Cuota de IVA real)
            baseImponibleTotal = baseImponibleTotal.add(baseLinea);
            cuotaIvaTotal = cuotaIvaTotal.add(ivaLinea);
        }

        // 5. Asignar Totales a la Venta
        venta.setBaseImponibleTotal(baseImponibleTotal);
        venta.setCuotaIvaTotal(cuotaIvaTotal);
        venta.setTotal(baseImponibleTotal.add(cuotaIvaTotal));

        // Por defecto será F2 (Ticket) a menos que te pasen NIF/Nombre desde el cliente
        if(venta.getTipoFactura() == null) {
            venta.setTipoFactura("F2");
        }

        // 6. Generar el Hash VeriFactu REAL (Criptográfico)
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

        // 7. Guardar en Base de Datos
        Venta ventaGuardada = ventaRepository.save(venta);

        for (LineaVenta linea : lineas) {
            linea.setVenta(ventaGuardada);
            lineaVentaRepository.save(linea);
        }

        // 8. Generar el XML Oficial
        String xmlFactura = xmlService.generarXmlAltaFactura(ventaGuardada, lineas, negocio);

        // 9. Validar el XML generado
        boolean esValido = xmlService.validarXmlContraEsquema(xmlFactura);
        if (!esValido) {
            throw new RuntimeException("El XML generado no cumple con el formato de la AEAT.");
        }

        // Para esto necesitas la ruta de un certificado real o de prueba en tu PC
        String rutaCert = "C:/ruta/a/tu/certificado_prueba.p12";
        String passCert = "tu_contraseña_del_certificado";

        String xmlFirmado = firmaDigitalService.firmarXml(xmlFactura, rutaCert, passCert);

        System.out.println("=== XML VERIFACTU FIRMADO ===");
        System.out.println(xmlFirmado);
        System.out.println("=============================");

        return ventaGuardada;
    }

    private String generarSiguienteNumeroFactura() {
        int anioActual = LocalDate.now().getYear();
        Optional<Venta> ultimaVentaOpt = ventaRepository.findFirstByOrderByIdDesc();

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
            fin = LocalDateTime.of(año, 12, 31, 23, 59, 59, 999999999);
        } else if (dia == null) {
            YearMonth yearMonth = YearMonth.of(año, mes);
            inicio = yearMonth.atDay(1).atStartOfDay();
            fin = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);
        } else {
            inicio = LocalDateTime.of(año, mes, dia, 0, 0, 0);
            fin = LocalDateTime.of(año, mes, dia, 23, 59, 59, 999999999);
        }

        List<Venta> ventasEntidad = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);

        return ventasEntidad.stream()
                .map(venta -> VentaListadoDTO.builder()
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
                        .build())
                .toList();
    }

    // ... dentro de tu VentaService

    public EstadisticasDTO obtenerEstadisticasDePeriodo(Integer año, Integer mes, Integer dia) {
        // 1. Reutilizamos tu método existente para obtener la lista de ventas
        List<VentaListadoDTO> ventas = obtenerVentasPorPeriodo(año, mes, dia);

        // 2. Si no hay ventas, devolvemos un DTO vacío para que React no explote
        if (ventas.isEmpty()) {
            return EstadisticasDTO.builder()
                    .totalFacturado(BigDecimal.ZERO)
                    .totalTickets(0L)
                    .ticketMedio(BigDecimal.ZERO)
                    .ventasPorMetodoPago(new java.util.HashMap<>())
                    .productosVendidos(0L)
                    .build();
        }

        // 3. Calculamos los totales usando la lista que ya recuperamos
        BigDecimal totalFacturado = ventas.stream()
                .map(VentaListadoDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalTickets = (long) ventas.size();

        BigDecimal ticketMedio = totalFacturado.divide(
                new BigDecimal(totalTickets), 2, java.math.RoundingMode.HALF_UP
        );

        // 4. Agrupamos por método de pago (EFECTIVO, TARJETA...)
        java.util.Map<String, BigDecimal> ventasPorMetodo = ventas.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        VentaListadoDTO::getMetodoPago,
                        java.util.stream.Collectors.reducing(
                                BigDecimal.ZERO,
                                VentaListadoDTO::getTotal,
                                BigDecimal::add
                        )
                ));

        // 5. Para los productos vendidos, como el DTO de listado no tiene las líneas,
        // necesitamos una pequeña consulta extra o calcularlo desde las entidades
        // (Opcional: puedes dejarlo en 0 si no lo necesitas mostrar ahora)
        Long productosVendidos = 0L;

        return EstadisticasDTO.builder()
                .totalFacturado(totalFacturado)
                .totalTickets(totalTickets)
                .ticketMedio(ticketMedio)
                .ventasPorMetodoPago(ventasPorMetodo)
                .productosVendidos(productosVendidos)
                .build();
    }
}