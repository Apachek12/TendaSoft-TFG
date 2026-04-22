package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos); // Devuelve un código 200 OK y la lista
    }

    @GetMapping("/{codigoBarras}")
    public ResponseEntity<Producto> buscarPorCodigo(@PathVariable String codigoBarras) {
        Producto producto = productoService.buscarPorCodigo(codigoBarras);
        return ResponseEntity.ok(producto);
    }

    // Sustituye tu método crearProducto por este:
    @PostMapping(consumes = { org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Producto> crearProducto(
            @RequestParam("nombre") String nombre,
            @RequestParam("codigoBarras") String codigoBarras,
            @RequestParam("precio") BigDecimal precio,
            @RequestParam("unidades") Integer unidades,
            @RequestParam("porcentajeIva") BigDecimal porcentajeIva,
            @RequestParam("idCategoria") Integer idCategoria,
            @RequestParam(value = "imagen", required = false) org.springframework.web.multipart.MultipartFile imagen) {

        // Llamamos a un nuevo método en el service que gestione esto
        Producto nuevo = productoService.guardarProductoConImagen(
                nombre, codigoBarras, precio, unidades, porcentajeIva, idCategoria, imagen);

        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PutMapping("/{codigoBarras}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable String codigoBarras,
            @RequestBody Producto producto) {

        // Por seguridad, forzamos que el producto a actualizar tenga el código de la URL
        producto.setCodigoBarras(codigoBarras);
        Producto productoActualizado = productoService.actualizarProducto(producto);
        return ResponseEntity.ok(productoActualizado);
    }
    @DeleteMapping("/{codigoBarras}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String codigoBarras) {
        productoService.eliminarProducto(codigoBarras);
        return ResponseEntity.noContent().build(); // Devuelve un 204 No Content (Borrado exitoso, sin devolver datos)
    }
}