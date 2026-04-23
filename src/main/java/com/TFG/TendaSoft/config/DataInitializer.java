package com.TFG.TendaSoft.config;

import com.TFG.TendaSoft.model.*;
import com.TFG.TendaSoft.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
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
    private final DatosNegocioRepository datosNegocioRepository;

    @Override
    @Transactional
    public void run(String... args) {

        // 1. OBTENER RUTA DINÁMICA DEL PROYECTO (Opción 1: Portabilidad total)
        String directorioRaiz = System.getProperty("user.dir");
        String nombreCertificado = "certificado_test.p12";
        String rutaCompletaCert = directorioRaiz + File.separator + nombreCertificado;

        // 2. DATOS DEL NEGOCIO (Configuración VeriFactu)
        DatosNegocio negocio = new DatosNegocio();
        negocio.setNombreEmpresa("TendaSoft S.L.");
        negocio.setCif("B12345678");
        negocio.setDireccion("Calle de prueba");
        negocio.setMensajeTicket("¡Gracias por su compra!");
        negocio.setVerifactuActivado(true);
        negocio.setRutaCertificado(rutaCompletaCert);

        datosNegocioRepository.save(negocio);

        // 3. USUARIOS
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

        // 4. CATEGORÍAS
        Categoria catBebidas = new Categoria();
        catBebidas.setNombre("Bebidas");
        catBebidas.setOrden(1);
        categoriaRepository.save(catBebidas);

        Categoria catAlimentacion = new Categoria();
        catAlimentacion.setNombre("Alimentación");
        catAlimentacion.setOrden(2);
        categoriaRepository.save(catAlimentacion);

        // 5. PRODUCTOS
        Producto p1 = new Producto();
        p1.setCodigoBarras("8412345678901");
        p1.setNombre("Coca-Cola 33cl");
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

        Producto p3 = new Producto();
        p2.setCodigoBarras("8412345678903");
        p2.setNombre("Pepsi Max 33cl");
        p2.setPrecio(new BigDecimal("2.10"));
        p2.setUnidades(40);
        p2.setPorcentajeIva(new BigDecimal("10.00"));
        p2.setCategoria(catAlimentacion);
        productoRepository.save(p2);

        // 6. VENTA DE PRUEBA
        Venta v = new Venta();
        v.setFecha(LocalDateTime.now());
        v.setUsuario(admin);
        v.setMetodoPago("EFECTIVO");
        v.setTipoFactura("F1");
        v.setNumeroFactura("FAC-2026-0001");
        v.setEstadoVerifactu("PENDIENTE");
        v.setHashVerifactu("SIMULATED_HASH_654321");

        v.setTotal(new BigDecimal("3.60"));
        v.setBaseImponibleTotal(new BigDecimal("3.10"));
        v.setCuotaIvaTotal(new BigDecimal("0.50"));

        v.setLineas(new ArrayList<>());

        LineaVenta lv1 = new LineaVenta();
        lv1.setProducto(p1);
        lv1.setNombreProducto(p1.getNombre());
        lv1.setCantidad(1);
        lv1.setPrecioUnitario(new BigDecimal("1.24"));
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


        Venta v2 = new Venta();
        v2.setFecha(LocalDateTime.now().minusHours(2)); // Hace 2 horas
        v2.setUsuario(vendedor);
        v2.setMetodoPago("TARJETA");
        v2.setTipoFactura("F1");
        v2.setNumeroFactura("FAC-2026-0002");
        v2.setEstadoVerifactu("CORRECTO"); // <--- ESTADO CORRECTO
        v2.setHashVerifactu("HASH_OK_987654321");
        v2.setHashAnterior("SIMULATED_HASH_654321"); // Enlace con la factura anterior

        v2.setTotal(new BigDecimal("12.50"));
        v2.setBaseImponibleTotal(new BigDecimal("10.33"));
        v2.setCuotaIvaTotal(new BigDecimal("2.17"));

        v2.setLineas(new ArrayList<>());

        LineaVenta lv3 = new LineaVenta();
        lv3.setProducto(p1);
        lv3.setNombreProducto(p1.getNombre());
        lv3.setCantidad(5); // Varias unidades del mismo producto
        lv3.setPrecioUnitario(new BigDecimal("2.06"));
        lv3.setPorcentajeIva(new BigDecimal("21.00"));
        lv3.setImporteIva(new BigDecimal("2.17"));
        lv3.setVenta(v2);
        v2.getLineas().add(lv3);

        Venta v3 = new Venta();
        v3.setFecha(LocalDateTime.now());
        v3.setUsuario(admin);
        v3.setMetodoPago("EFECTIVO");
        v3.setTipoFactura("F1");
        v3.setNumeroFactura("FAC-2026-0003");
        v3.setEstadoVerifactu("FIRMADO_Y_PENDIENTE_ENVIO"); // <--- ESTADO PENDIENTE DE ENVÍO
        v3.setHashVerifactu("PENDING_HASH_003");
        v3.setHashAnterior("HASH_OK_987654321"); // Enlace con la anterior (v2)

        v3.setTotal(new BigDecimal("5.50"));
        v3.setBaseImponibleTotal(new BigDecimal("5.00"));
        v3.setCuotaIvaTotal(new BigDecimal("0.50"));

        v3.setLineas(new ArrayList<>());

        LineaVenta lv4 = new LineaVenta();
        lv4.setProducto(p2);
        lv4.setNombreProducto(p2.getNombre());
        lv4.setCantidad(2);
        lv4.setPrecioUnitario(new BigDecimal("2.50"));
        lv4.setPorcentajeIva(new BigDecimal("10.00"));
        lv4.setImporteIva(new BigDecimal("0.50"));
        lv4.setVenta(v3);
        v3.getLineas().add(lv4);

        ventaRepository.save(v);
        ventaRepository.save(v2);
        ventaRepository.save(v3);
    }
}