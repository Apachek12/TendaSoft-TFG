package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController indica que esta clase responderá con datos JSON (ideal para APIs y React)
@RestController
// @RequestMapping define la URL base para todos los métodos de esta clase
@RequestMapping("/api/productos")
// @CrossOrigin permite que tu frontend en React (que estará en otro puerto, ej. 3000) pueda conectarse sin que el navegador lo bloquee por seguridad (CORS)
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // 1. LEER TODOS (Petición GET a http://localhost:8080/api/productos)
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos); // Devuelve un código 200 OK y la lista
    }

    // 2. LEER UNO SOLO (Petición GET a http://localhost:8080/api/productos/123456)
    // El {codigoBarras} de la URL se inyecta en la variable gracias a @PathVariable
    @GetMapping("/{codigoBarras}")
    public ResponseEntity<Producto> buscarPorCodigo(@PathVariable String codigoBarras) {
        Producto producto = productoService.buscarPorCodigo(codigoBarras);
        return ResponseEntity.ok(producto);
    }

    // 3. CREAR (Petición POST a http://localhost:8080/api/productos)
    // @RequestBody coge el JSON que te envíe React y lo transforma en un objeto Producto de Java
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = productoService.guardarProducto(producto);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED); // Devuelve un 201 CREATED
    }

    // 4. ACTUALIZAR (Petición PUT a http://localhost:8080/api/productos/123456)
    @PutMapping("/{codigoBarras}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable String codigoBarras,
            @RequestBody Producto producto) {

        // Por seguridad, forzamos que el producto a actualizar tenga el código de la URL
        producto.setCodigoBarras(codigoBarras);
        Producto productoActualizado = productoService.actualizarProducto(producto);
        return ResponseEntity.ok(productoActualizado);
    }

    // 5. BORRAR (Petición DELETE a http://localhost:8080/api/productos/123456)
    @DeleteMapping("/{codigoBarras}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String codigoBarras) {
        productoService.eliminarProducto(codigoBarras);
        return ResponseEntity.noContent().build(); // Devuelve un 204 No Content (Borrado exitoso, sin devolver datos)
    }
}