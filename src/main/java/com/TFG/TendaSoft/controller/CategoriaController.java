package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.dto.ImportarProductosDto;
import com.TFG.TendaSoft.model.Categoria;
import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.repository.CategoriaRepository;
import com.TFG.TendaSoft.repository.ProductoRepository;
import com.TFG.TendaSoft.service.CategoriaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @GetMapping
    public ResponseEntity<List<Categoria>> obtenerTodas() {
        return ResponseEntity.ok(categoriaService.obtenerTodasLasCategorias());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@RequestBody Categoria categoria) {
        return new ResponseEntity<>(categoriaService.crearCategoria(categoria), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(
            @PathVariable Integer id,
            @RequestBody Categoria categoriaDetails) {

        // 1. Buscamos la categoría existente
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        // 2. Actualizamos el nombre
        categoria.setNombre(categoriaDetails.getNombre());

        // 3. Actualizamos el padre (si es que ahora es subcategoría o ha cambiado de padre)
        categoria.setCategoriaPadre(categoriaDetails.getCategoriaPadre());

        // 4. Guardamos
        Categoria categoriaActualizada = categoriaRepository.save(categoria);
        return ResponseEntity.ok(categoriaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/importar-productos")
    @Transactional
    public ResponseEntity<?> importarProductos(
            @PathVariable Integer id,
            @RequestBody ImportarProductosDto dto) {

        try {
            Categoria categoria = categoriaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

            // Buscamos los productos por la lista de códigos recibida
            List<Producto> productosAAñadir = productoRepository.findAllById(dto.getCodigosBarras());

            if (productosAAñadir.isEmpty()) {
                return ResponseEntity.badRequest().body("No se seleccionaron productos válidos.");
            }

            // Actualizamos la relación en cada producto
            for (Producto p : productosAAñadir) {
                p.setCategoria(categoria);
            }

            // Al estar bajo @Transactional y haber seteado la categoría en los productos,
            // Hibernate guardará los cambios automáticamente al terminar el método.
            productoRepository.saveAll(productosAAñadir);

            return ResponseEntity.ok("Productos importados correctamente a " + categoria.getNombre());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en la importación: " + e.getMessage());
        }
    }
}
