package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.Categoria;
import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.repository.CategoriaRepository;
import com.TFG.TendaSoft.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    public Producto buscarPorCodigo(String codigoBarras) {
        return productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + codigoBarras));
    }

    @Transactional
    public Producto guardarProductoConImagen(String nombre, String codigoBarras, BigDecimal precio,
                                             Integer unidades, BigDecimal porcentajeIva,
                                             Integer idCategoria, MultipartFile imagen) {
        if (productoRepository.existsById(codigoBarras)) {
            throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + codigoBarras);
        }

        Categoria cat = buscarCategoria(idCategoria);

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setCodigoBarras(codigoBarras);
        producto.setPrecio(precio);
        producto.setUnidades(unidades);
        producto.setPorcentajeIva(porcentajeIva);
        producto.setCategoria(cat);

        if (imagen != null && !imagen.isEmpty()) {
            producto.setUrlImagen(guardarImagen(codigoBarras, imagen));
        }

        validarDatosProducto(producto);
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizarProductoConImagen(String codigoBarras, String nombre, BigDecimal precio,
                                                Integer unidades, BigDecimal porcentajeIva,
                                                Integer idCategoria, MultipartFile imagen) {
        Producto existente = buscarPorCodigo(codigoBarras);
        Categoria cat = buscarCategoria(idCategoria);

        existente.setNombre(nombre);
        existente.setPrecio(precio);
        existente.setUnidades(unidades);
        existente.setPorcentajeIva(porcentajeIva);
        existente.setCategoria(cat);

        if (imagen != null && !imagen.isEmpty()) {
            existente.setUrlImagen(guardarImagen(codigoBarras, imagen));
        }

        validarDatosProducto(existente);
        return productoRepository.save(existente);
    }

    private Categoria buscarCategoria(Integer idCategoria) {
        return categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + idCategoria));
    }

    private String guardarImagen(String codigoBarras, MultipartFile imagen) {
        try {
            new File("uploads").mkdirs();
            String nombreArchivo = codigoBarras + "_" + imagen.getOriginalFilename().replaceAll("\\s+", "_");
            Path ruta = Paths.get("uploads" + File.separator + nombreArchivo);
            Files.write(ruta, imagen.getBytes());
            return nombreArchivo;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen en el servidor.", e);
        }
    }

    private void validarDatosProducto(Producto producto) {
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio debe ser un valor válido mayor o igual a 0.");
        }
        if (producto.getUnidades() == null || producto.getUnidades() < 0) {
            throw new IllegalArgumentException("Las unidades deben ser un valor válido mayor o igual a 0.");
        }
    }
}