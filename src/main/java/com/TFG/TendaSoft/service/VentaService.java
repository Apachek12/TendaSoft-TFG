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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final LineaVentaRepository lineaVentaRepository;
    private final ProductoRepository productoRepository;

    // @Transactional asegura que si algo falla, no se guarde nada a medias en la BD.
    @Transactional
    public Venta registrarNuevaVenta(Venta venta, List<LineaVenta> lineas) {
        // 1. GENERACIÓN AUTOMÁTICA DEL NÚMERO DE FACTURA
        int anioActual = LocalDate.now().getYear();
        Optional<Venta> ultimaVentaOpt = ventaRepository.findFirstByOrderByIdVentaDesc();

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
            // Si es la primerísima venta de la historia del sistema
            nuevoNumero = String.format("FAC-%d-0001", anioActual);
        }

        venta.setNumeroFactura(nuevoNumero);

        // 2. LÓGICA VERI*FACTU: Encadenamiento de Hash
        // El hash anterior es el "hashVerifactu" de la última venta, o un valor inicial si no hay
        String hashAnterior = ultimaVentaOpt.map(Venta::getHashVerifactu).orElse("INICIO-SISTEMA");
        venta.setHashAnterior(hashAnterior);
        venta.setFecha(LocalDateTime.now());

        // Generamos el hash actual (Simulado para TFG usando el hashCode de los datos clave)
        String datosParaHash = nuevoNumero + venta.getTotal().toString() + hashAnterior + venta.getFecha().toString();
        venta.setHashVerifactu(Integer.toHexString(datosParaHash.hashCode()));
        venta.setEstadoVerifactu("PENDIENTE_ENVIO");

        // 3. GUARDAR CABECERA DE LA VENTA
        // Es vital guardar la venta primero para que tenga un ID y las líneas puedan referenciarlo
        Venta ventaGuardada = ventaRepository.save(venta);

        // 4. PROCESAR LÍNEAS DE VENTA Y ACTUALIZAR STOCK
        for (LineaVenta linea : lineas) {
            // Buscamos el producto por su código de barras
            Producto producto = productoRepository.findById(linea.getProducto().getCodigoBarras())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + linea.getProducto().getCodigoBarras()));

            // Verificamos stock (Usamos IllegalStateException para que nuestro GlobalExceptionHandler lo capture)
            if (producto.getUnidades() < linea.getCantidad()) {
                throw new IllegalStateException("No hay stock suficiente de: " + producto.getNombre() +
                        " (Stock actual: " + producto.getUnidades() + ")");
            }

            // Restamos el stock
            producto.setUnidades(producto.getUnidades() - linea.getCantidad());
            productoRepository.save(producto);

            // Configuramos la línea y la guardamos
            linea.setVenta(ventaGuardada);
            linea.setNombreProducto(producto.getNombre());
            lineaVentaRepository.save(linea);
        }

        return ventaGuardada;
    }
}