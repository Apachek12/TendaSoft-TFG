package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = productoService.guardarProducto(producto);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED); // Devuelve un 201 CREATED
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