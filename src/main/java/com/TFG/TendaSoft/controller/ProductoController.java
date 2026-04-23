package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.repository.ProductoRepository;
import com.TFG.TendaSoft.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoRepository productoRepository;

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{codigoBarras}")
    public ResponseEntity<Producto> buscarPorCodigo(@PathVariable String codigoBarras) {
        Producto producto = productoService.buscarPorCodigo(codigoBarras);
        return ResponseEntity.ok(producto);
    }

    // Método actualizado para recibir form-data (texto + imagen binaria)
    @PostMapping(consumes = { org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> crearProducto(
            @RequestParam("nombre") String nombre,
            @RequestParam("codigoBarras") String codigoBarras,
            @RequestParam("precio") BigDecimal precio,
            @RequestParam("unidades") Integer unidades,
            @RequestParam("porcentajeIva") BigDecimal porcentajeIva,
            @RequestParam("idCategoria") Integer idCategoria,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

        try {
            // Delegamos toda la lógica de guardado físico y de base de datos al servicio
            Producto nuevo = productoService.guardarProductoConImagen(
                    nombre, codigoBarras, precio, unidades, porcentajeIva, idCategoria, imagen);

            return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
        } catch (Exception e) {
            // Si algo falla, devolvemos un 400 Bad Request para que React muestre el error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(value = "/{codigoBarras}", consumes = { org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> actualizarProducto(
            @PathVariable String codigoBarras,
            @RequestParam("nombre") String nombre,
            @RequestParam("precio") BigDecimal precio,
            @RequestParam("unidades") Integer unidades,
            @RequestParam("porcentajeIva") BigDecimal porcentajeIva,
            @RequestParam("idCategoria") Integer idCategoria,
            @RequestParam(value = "imagen", required = false) org.springframework.web.multipart.MultipartFile imagen) {

        try {
            // Delegamos la actualización al servicio
            Producto actualizado = productoService.actualizarProductoConImagen(
                    codigoBarras, nombre, precio, unidades, porcentajeIva, idCategoria, imagen);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al actualizar: " + e.getMessage());
        }
    }

    @DeleteMapping("/{codigoBarras}")
    public ResponseEntity<?> toggleEstadoProducto(@PathVariable String codigoBarras) {
        return productoRepository.findById(codigoBarras).map(producto -> {
            // Si no tiene el campo 'activo' inicializado, asumimos que es true.
            // Si es true, lo pasamos a false. Si es false, lo pasamos a true.
            boolean estadoActual = producto.getActivo() != null ? producto.getActivo() : true;

            producto.setActivo(!estadoActual);
            productoRepository.save(producto);

            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

}