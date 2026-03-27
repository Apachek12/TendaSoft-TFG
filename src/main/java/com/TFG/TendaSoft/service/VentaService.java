package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.dto.VentaListadoDTO;
import com.TFG.TendaSoft.model.LineaVenta;
import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.model.Venta;
import com.TFG.TendaSoft.repository.LineaVentaRepository;
import com.TFG.TendaSoft.repository.ProductoRepository;
import com.TFG.TendaSoft.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
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
    @Transactional
    public Venta registrarNuevaVenta(Venta venta, List<LineaVenta> lineas) {
        int anioActual = LocalDate.now().getYear();
        Optional<Venta> ultimaVentaOpt = ventaRepository.findFirstByOrderByIdDesc();

        String nuevoNumero;
        if (ultimaVentaOpt.isPresent()) {
            String ultimoNumStr = ultimaVentaOpt.get().getNumeroFactura(); // Ejemplo: "FAC-2026-0005"
            try {
                String[] partes = ultimoNumStr.split("-");
                // Tomamos la última parte (0005), la pasamos a número y sumamos 1
                int ultimoSecuencial = Integer.parseInt(partes[2]);
                nuevoNumero = String.format("FAC-%d-%04d", anioActual, ultimoSecuencial + 1);
            } catch (Exception e) {
                // Si el formato anterior fuera incompatible, empezamos de nuevo para este año
                nuevoNumero = String.format("FAC-%d-0001", anioActual);
            }
        } else {
            // Si es la primera venta de la historia del sistema
            nuevoNumero = String.format("FAC-%d-0001", anioActual);
        }

        venta.setNumeroFactura(nuevoNumero);

        // El hash anterior es el "hashVerifactu" de la última venta, o un valor inicial si no hay
        String hashAnterior = ultimaVentaOpt.map(Venta::getHashVerifactu).orElse("INICIO-SISTEMA");
        venta.setHashAnterior(hashAnterior);
        venta.setFecha(LocalDateTime.now());

        // Generamos el hash actual
        String datosParaHash = nuevoNumero + venta.getTotal().toString() + hashAnterior + venta.getFecha().toString();
        venta.setHashVerifactu(Integer.toHexString(datosParaHash.hashCode()));
        venta.setEstadoVerifactu("PENDIENTE_ENVIO");

        Venta ventaGuardada = ventaRepository.save(venta);

        for (LineaVenta linea : lineas) {
            Producto producto = productoRepository.findById(linea.getProducto().getCodigoBarras())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + linea.getProducto().getCodigoBarras()));

            if (producto.getUnidades() < linea.getCantidad()) {
                throw new IllegalStateException("No hay stock suficiente de: " + producto.getNombre() +
                        " (Stock actual: " + producto.getUnidades() + ")");
            }

            producto.setUnidades(producto.getUnidades() - linea.getCantidad());
            productoRepository.save(producto);

            linea.setVenta(ventaGuardada);
            linea.setNombreProducto(producto.getNombre());
            lineaVentaRepository.save(linea);
        }

        return ventaGuardada;
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
                        .build())
                .toList();
    }
}