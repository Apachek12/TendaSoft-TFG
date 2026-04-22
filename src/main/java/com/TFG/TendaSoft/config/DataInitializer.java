package com.TFG.TendaSoft.config;

import com.TFG.TendaSoft.model.*;
import com.TFG.TendaSoft.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    @Override
    @Transactional
    public void run(String... args) {

        // 1. USUARIOS
        Usuario admin = new Usuario();
        admin.setNombreReal("Fran García");
        admin.setNombreUsuario("admin");
        admin.setHashContrasena("1234");
        admin.setRol("ADMIN");
        usuarioRepository.save(admin);

        Usuario vendedor = new Usuario();
        vendedor.setNombreReal("Marta TPV");
        vendedor.setNombreUsuario("vendedor");
        vendedor.setHashContrasena("1234");
        vendedor.setRol("VENDEDOR");
        usuarioRepository.save(vendedor);

        // 2. CATEGORÍAS
        Categoria catBebidas = new Categoria();
        catBebidas.setNombre("Bebidas");
        catBebidas.setOrden(1);
        categoriaRepository.save(catBebidas);

        Categoria catAlimentacion = new Categoria();
        catAlimentacion.setNombre("Alimentación");
        catAlimentacion.setOrden(2);
        categoriaRepository.save(catAlimentacion);

        // 3. PRODUCTOS
        Producto p1 = new Producto();
        p1.setCodigoBarras("8412345678901");
        p1.setNombre("Coca-Cola Original 33cl");
        p1.setPrecio(new BigDecimal("1.50"));
        p1.setUnidades(100);
        p1.setPorcentajeIva(new BigDecimal("21.00"));
        p1.setCategoria(catBebidas);
        productoRepository.save(p1);

        Producto p2 = new Producto();
        p2.setCodigoBarras("8412345678902");
        p2.setNombre("Patatas Chips Sal 150g");
        p2.setPrecio(new BigDecimal("2.10"));
        p2.setUnidades(50);
        p2.setPorcentajeIva(new BigDecimal("10.00"));
        p2.setCategoria(catAlimentacion);
        productoRepository.save(p2);

        // 4. CREAR UNA VENTA DE PRUEBA (Simulando VeriFactu)
        Venta v = new Venta();
        v.setFecha(LocalDateTime.now());
        v.setUsuario(admin);
        v.setMetodoPago("EFECTIVO");
        v.setTipoFactura("F1"); // Factura ordinaria
        v.setNumeroFactura("FAC-2024-0001");
        v.setEstadoVerifactu("PENDIENTE");
        v.setHashVerifactu("simulated_hash_123456");

        // Totales de la venta
        v.setTotal(new BigDecimal("3.60"));
        v.setBaseImponibleTotal(new BigDecimal("3.10"));
        v.setCuotaIvaTotal(new BigDecimal("0.50"));

        // Líneas de la venta
        v.setLineas(new ArrayList<>());

        LineaVenta lv1 = new LineaVenta();
        lv1.setProducto(p1);
        lv1.setNombreProducto(p1.getNombre());
        lv1.setCantidad(1);
        lv1.setPrecioUnitario(new BigDecimal("1.24")); // Base imponible
        lv1.setPorcentajeIva(new BigDecimal("21.00"));
        lv1.setImporteIva(new BigDecimal("0.26"));
        lv1.setVenta(v);
        v.getLineas().add(lv1);

        LineaVenta lv2 = new LineaVenta();
        lv2.setProducto(p2);
        lv2.setNombreProducto(p2.getNombre());
        lv2.setCantidad(1);
        lv2.setPrecioUnitario(new BigDecimal("1.91"));
        lv2.setPorcentajeIva(new BigDecimal("10.00"));
        lv2.setImporteIva(new BigDecimal("0.19"));
        lv2.setVenta(v);
        v.getLineas().add(lv2);

        ventaRepository.save(v);

        System.out.println(">>> Base de datos inicializada con datos VeriFactu y Ventas.");
    }
}