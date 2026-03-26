package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.LineaVenta;
import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.model.Venta;
import com.TFG.TendaSoft.repository.LineaVentaRepository;
import com.TFG.TendaSoft.repository.ProductoRepository;
import com.TFG.TendaSoft.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ¡IMPORTANTE!

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final LineaVentaRepository lineaVentaRepository;
    private final ProductoRepository productoRepository;

    // @Transactional asegura que si algo falla, no se guarde nada a medias en la BD.
    @Transactional
    public Venta registrarNuevaVenta(Venta venta, List<LineaVenta> lineas) {

        // 1. Configuramos los datos automáticos de la venta
        venta.setFecha(LocalDateTime.now());

        // Simulación del Hash VeriFactu (Aquí iría tu lógica real criptográfica)
        venta.setHashVerifactu("HASH_SIMULADO_" + System.currentTimeMillis());
        venta.setEstadoVerifactu("PENDIENTE");

        // 2. Guardamos la cabecera de la venta primero para que MySQL le asigne un ID
        Venta ventaGuardada = ventaRepository.save(venta);

        // 3. Procesamos cada línea del ticket
        for (LineaVenta linea : lineas) {
            // Vinculamos la línea a la venta que acabamos de guardar
            linea.setVenta(ventaGuardada);

            // Buscamos el producto en la BD para restarle el stock
            Producto productoDB = productoRepository.findById(linea.getProducto().getCodigoBarras())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado en la venta"));

            if (productoDB.getUnidades() < linea.getCantidad()) {
                throw new IllegalArgumentException("No hay stock suficiente para " + productoDB.getNombre());
            }

            // Restamos el stock
            productoDB.setUnidades(productoDB.getUnidades() - linea.getCantidad());
            productoRepository.save(productoDB);

            // Guardamos la línea de venta
            lineaVentaRepository.save(linea);
        }

        return ventaGuardada;
    }
}