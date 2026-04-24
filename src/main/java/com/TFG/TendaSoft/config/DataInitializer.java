package com.TFG.TendaSoft.config;

import com.TFG.TendaSoft.model.*;
import com.TFG.TendaSoft.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final DatosNegocioRepository datosNegocioRepository;
    private final VentaRepository ventaRepository;

    @Override
    @Transactional
    public void run(String... args) {

        // 1. REGISTRO BASE DE NEGOCIO
        if (datosNegocioRepository.count() == 0) {
            DatosNegocio negocio = new DatosNegocio();
            negocio.setNombreEmpresa("EIDAS CERTIFICADO PRUEBAS - 99999972C");
            negocio.setDireccion("Calle");
            negocio.setCif("A39200019");
            negocio.setVerifactuActivado(false);
            negocio.setMensajeTicket("¡Gracias por su visita!");
            datosNegocioRepository.save(negocio);
        }

        // 2. USUARIOS
        if (usuarioRepository.count() == 0) {
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
        }

        // 3. CATEGORÍAS Y PRODUCTOS
        if (categoriaRepository.count() == 0) {
            Categoria catBebidas = new Categoria();
            catBebidas.setNombre("Bebidas");
            catBebidas.setOrden(1);
            categoriaRepository.save(catBebidas);

            Categoria catAlimentacion = new Categoria();
            catAlimentacion.setNombre("Alimentación");
            catAlimentacion.setOrden(2);
            categoriaRepository.save(catAlimentacion);

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
            p3.setCodigoBarras("8412345678903");
            p3.setNombre("Pepsi Max 33cl");
            p3.setPrecio(new BigDecimal("2.10"));
            p3.setUnidades(40);
            p3.setPorcentajeIva(new BigDecimal("21.00"));
            p3.setCategoria(catBebidas);
            productoRepository.save(p3);
        }

        // 4. VENTA ANCLA DE ENCADENAMIENTO VERIFACTU
        // Representa la última factura aceptada por la AEAT en el entorno de pruebas.
        // Necesaria para que la próxima factura encadene correctamente en lugar de
        // intentar ser PrimerRegistro (error 2007).
        // IMPORTANTE: Actualiza el numeroFactura y hashVerifactu cada vez que
        // reinicies la BD, con los valores de la última factura aceptada por la AEAT.
        boolean existeAncla = ventaRepository
                .findFirstByNumeroFacturaStartingWithOrderByIdDesc("FAC-2026-0006")
                .isPresent();

        if (!existeAncla) {
            Usuario admin = usuarioRepository.findByNombreUsuario("admin")
                    .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

            Venta ancla = new Venta();
            ancla.setNumeroFactura("FAC-2026-0034");
            ancla.setFecha(LocalDateTime.of(2026, 4, 24, 14, 55, 28));
            ancla.setTotal(new BigDecimal("1.50"));
            ancla.setBaseImponibleTotal(new BigDecimal("1.24"));
            ancla.setCuotaIvaTotal(new BigDecimal("0.26"));
            ancla.setTipoFactura("F2");
            ancla.setMetodoPago("EFECTIVO");
            ancla.setUsuario(admin);
            // Hash de FAC-2026-0004, aceptada como Correcto por la AEAT
            ancla.setHashVerifactu("6E2E23BC818B2217DAF31014FA6044FE4C0B6B7611A71A3BEABBE234E3BA9150");
            ancla.setHashAnterior("");
            ancla.setEstadoVerifactu("CORRECTO");
            // Campos de encadenamiento — esta es la primera de la cadena
            ancla.setNumeroFacturaAnterior(null);
            ancla.setFechaAnterior(null);
            ancla.setCifEmisorAnterior(null);
            ventaRepository.save(ancla);

            System.out.println(">>> Ancla insertada con numero: " + ancla.getNumeroFactura());
        }

        System.out.println("Datos base inicializados: Sistema listo para configuración de usuario.");
    }
}