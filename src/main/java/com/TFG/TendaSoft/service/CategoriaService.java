package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.Categoria;
import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.repository.CategoriaRepository;
import com.TFG.TendaSoft.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public List<Categoria> obtenerTodasLasCategorias() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe la categoría con ID: " + id));
    }

    public Categoria crearCategoria(Categoria categoria) {
        if (categoria.getId() != null) {
            throw new IllegalArgumentException("El ID debe estar vacío al crear una categoría nueva.");
        }
        validarDatosCategoria(categoria);
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizarCategoria(Categoria categoria) {
        if (categoria.getId() == null || !categoriaRepository.existsById(categoria.getId())) {
            throw new IllegalArgumentException("La categoría no existe.");
        }
        validarDatosCategoria(categoria);
        return categoriaRepository.save(categoria);
    }

    public void eliminarCategoria(Integer id) {
        Categoria categoriaAEliminar = buscarPorId(id);

        if (categoriaAEliminar.getNombre().equalsIgnoreCase("Sin categoría")) {
            throw new IllegalArgumentException("No puedes eliminar la categoría comodín del sistema.");
        }

        // Si la categoría tiene productos, los reasignamos a "Sin categoría" (creándola si no existe)
        Categoria categoriaPorDefecto = categoriaRepository.findByNombre("Sin categoría")
                .orElseGet(() -> {
                    Categoria nueva = new Categoria();
                    nueva.setNombre("Sin categoría");
                    nueva.setOrden(0);
                    return categoriaRepository.save(nueva);
                });

        List<Producto> productosAfectados = productoRepository.findByCategoria(categoriaAEliminar);
        if (!productosAfectados.isEmpty()) {
            productosAfectados.forEach(p -> p.setCategoria(categoriaPorDefecto));
            productoRepository.saveAll(productosAfectados);
        }

        categoriaRepository.delete(categoriaAEliminar);
    }

    private void validarDatosCategoria(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        if (categoria.getOrden() == null || categoria.getOrden() < 0) {
            throw new IllegalArgumentException("El orden debe ser un número válido (0 o mayor).");
        }
        if (categoria.getCategoriaPadre() != null && categoria.getId() != null
                && categoria.getId().equals(categoria.getCategoriaPadre().getId())) {
            throw new IllegalArgumentException("Una categoría no puede ser subcategoría de sí misma.");
        }
    }
}