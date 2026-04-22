package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.repository.CategoriaRepository;
import com.TFG.TendaSoft.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public List<Producto> obtenerTodosLosProductos(){
        return productoRepository.findAll();
    }

    public Producto buscarPorCodigo(String codigoBarras){
        return productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new RuntimeException("No existe el producto introducido."));
    }

    public Producto guardarProducto (Producto producto){
        if(productoRepository.existsById(producto.getCodigoBarras())){
            throw new IllegalArgumentException("Ya existe un producto con el código de barras indicado.");
        }

        validarDatosProducto(producto);

        return productoRepository.save(producto);
    }

    public Producto guardarProductoConImagen(String nombre, String codigoBarras, BigDecimal precio,
                                             Integer unidades, BigDecimal porcentajeIva,
                                             Integer idCategoria, org.springframework.web.multipart.MultipartFile imagen) {

        // 1. Buscamos la categoría
        com.TFG.TendaSoft.model.Categoria cat = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 2. Creamos el objeto Producto
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setCodigoBarras(codigoBarras);
        p.setPrecio(precio);
        p.setUnidades(unidades);
        p.setPorcentajeIva(porcentajeIva);
        p.setCategoria(cat);

        // 3. Lógica para guardar la imagen (opcional para el TFG)
        if (imagen != null && !imagen.isEmpty()) {
            // Aquí podrías guardar el archivo en una carpeta y setear la URL
            // p.setUrlImagen("ruta/al/archivo/" + imagen.getOriginalFilename());
        }

        return guardarProducto(p); // Llama a tu método de validación y save que ya tenías
    }

    public Producto actualizarProducto (Producto producto){
        if(!productoRepository.existsById(producto.getCodigoBarras())){
            throw new IllegalArgumentException("El producto indicado no existe.");
        }

        validarDatosProducto(producto);

        return productoRepository.save(producto);
    }

    public void eliminarProducto (String codigoBarras){
        Producto existente = buscarPorCodigo(codigoBarras);

        productoRepository.delete(existente);
    }

    private void validarDatosProducto(Producto producto){
        if(producto.getPrecio() == null){
            throw new IllegalArgumentException("El precio del producto no puede estar vacío.");
        }

        if(producto.getPrecio().compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("El precio no puede ser menor que 0.");
        }

        if(producto.getUnidades() == null){
            throw new IllegalArgumentException("Las unidades del producto no puede estar vacío.");
        }

        if(producto.getUnidades() < 0){
            throw new IllegalArgumentException("Las unidades no pueden ser menor que 0.");
        }
    }
}