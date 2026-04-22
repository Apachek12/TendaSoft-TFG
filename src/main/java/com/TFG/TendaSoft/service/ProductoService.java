package com.TFG.TendaSoft.service;

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

    public List<Producto> obtenerTodosLosProductos(){
        return productoRepository.findAll();
    }

    public Producto buscarPorCodigo(String codigoBarras){
        return productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new RuntimeException("No existe el producto introducido."));
    }

    public Producto guardarProducto(Producto producto){
        if(productoRepository.existsById(producto.getCodigoBarras())){
            throw new IllegalArgumentException("Ya existe un producto con el código de barras indicado.");
        }

        validarDatosProducto(producto);

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto guardarProductoConImagen(String nombre, String codigoBarras, BigDecimal precio,
                                             Integer unidades, BigDecimal porcentajeIva,
                                             Integer idCategoria, MultipartFile imagen) {

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

        // 3. Lógica para guardar la imagen físicamente y almacenar la ruta lógica
        if (imagen != null && !imagen.isEmpty()) {
            try {
                // Asegurar que la carpeta 'uploads' existe en la raíz de tu proyecto
                File directorio = new File("uploads");
                if (!directorio.exists()) {
                    directorio.mkdirs();
                }

                // Generar un nombre único (ej: 8412345678901_botella_agua.jpg)
                // Reemplazamos los espacios del nombre original por guiones bajos por seguridad en URLs
                String nombreOriginalLimpio = imagen.getOriginalFilename().replaceAll("\\s+", "_");
                String nombreArchivo = codigoBarras + "_" + nombreOriginalLimpio;

                // Ruta final en el disco duro
                Path rutaCompleta = Paths.get("uploads" + File.separator + nombreArchivo);

                // Copiar los bytes del archivo al disco duro
                Files.write(rutaCompleta, imagen.getBytes());

                // Guardar SOLO el nombre lógico en la entidad
                p.setUrlImagen(nombreArchivo);

            } catch (IOException e) {
                // Si falla el guardado de la imagen, detenemos el proceso
                throw new RuntimeException("Error al guardar la imagen en el disco del servidor.", e);
            }
        }

        // 4. Llama a tu método existente (que valida y guarda en base de datos)
        return guardarProducto(p);
    }

    @Transactional
    public Producto actualizarProductoConImagen(String codigoBarras, String nombre, BigDecimal precio,
                                                Integer unidades, BigDecimal porcentajeIva,
                                                Integer idCategoria, MultipartFile imagen) {

        // 1. Buscamos el producto existente
        Producto existente = buscarPorCodigo(codigoBarras);

        // 2. Buscamos la nueva categoría
        com.TFG.TendaSoft.model.Categoria cat = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 3. Actualizamos los datos
        existente.setNombre(nombre);
        existente.setPrecio(precio);
        existente.setUnidades(unidades);
        existente.setPorcentajeIva(porcentajeIva);
        existente.setCategoria(cat);

        // 4. Si el usuario ha subido una foto NUEVA, la reemplazamos
        if (imagen != null && !imagen.isEmpty()) {
            try {
                File directorio = new File("uploads");
                if (!directorio.exists()) directorio.mkdirs();

                String nombreOriginalLimpio = imagen.getOriginalFilename().replaceAll("\\s+", "_");
                String nombreArchivo = codigoBarras + "_" + nombreOriginalLimpio;

                Path rutaCompleta = Paths.get("uploads" + File.separator + nombreArchivo);
                Files.write(rutaCompleta, imagen.getBytes());

                existente.setUrlImagen(nombreArchivo);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la nueva imagen.", e);
            }
        }

        // 5. Validamos y guardamos
        // No llamamos a guardarProducto() porque ese método comprueba si ya existe (y daría error).
        // Llamamos directamente a validar y al repositorio.
        validarDatosProducto(existente);
        return productoRepository.save(existente);
    }

    public void deshabilitarProducto(String codigoBarras) {
        Producto producto = productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        producto.setActivo(false); // Borrado lógico
        productoRepository.save(producto);
    }

    private void validarDatosProducto(Producto producto){
        if(producto.getPrecio() == null){
            throw new IllegalArgumentException("El precio del producto no puede estar vacío.");
        }

        if(producto.getPrecio().compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("El precio no puede ser menor que 0.");
        }

        if(producto.getUnidades() == null){
            throw new IllegalArgumentException("Las unidades del producto no pueden estar vacías.");
        }

        if(producto.getUnidades() < 0){
            throw new IllegalArgumentException("Las unidades no pueden ser menores que 0.");
        }
    }
}