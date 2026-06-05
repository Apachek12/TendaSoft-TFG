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
        inicializarNegocio();
        inicializarUsuarios();
        inicializarCatalogo();
        inicializarAnclaVerifactu();
    }

    private void inicializarNegocio() {
        if (datosNegocioRepository.count() > 0) return;

        DatosNegocio negocio = new DatosNegocio();
        negocio.setNombreEmpresa("EIDAS CERTIFICADO PRUEBAS - A39200019");
        negocio.setDireccion("Calle");
        negocio.setCif("A39200019");
        negocio.setVerifactuActivado(false);
        negocio.setMensajeTicket("¡Gracias por su visita!");
        datosNegocioRepository.save(negocio);
    }

    private void inicializarUsuarios() {
        if (usuarioRepository.count() > 0) return;

        Usuario admin = new Usuario();
        admin.setNombreReal("Fran García");
        admin.setNombreUsuario("admin");
        admin.setHashContrasena("$2a$10$yrFEuhF7QBc/tlbNYTlMi.cbFkccXhkoicFLzGIZJ58jZ.3t6nf6K");
        admin.setRol("ADMIN");
        usuarioRepository.save(admin);

        Usuario vendedor = new Usuario();
        vendedor.setNombreReal("Marta TPV");
        vendedor.setNombreUsuario("vendedor");
        vendedor.setHashContrasena("$2a$10$yrFEuhF7QBc/tlbNYTlMi.cbFkccXhkoicFLzGIZJ58jZ.3t6nf6K");
        vendedor.setRol("VENDEDOR");
        usuarioRepository.save(vendedor);
    }

    private void inicializarCatalogo() {
        if (categoriaRepository.count() > 0) return;

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

    private void inicializarAnclaVerifactu() {
        String numeroAncla = "FAC-2026-0073";

        // La comprobación debe buscar el mismo número que se va a insertar
        if (ventaRepository.findFirstByNumeroFacturaStartingWithOrderByIdDesc(numeroAncla).isPresent()) return;

        Usuario admin = usuarioRepository.findByNombreUsuario("admin")
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        Venta ancla = new Venta();
        ancla.setNumeroFactura(numeroAncla);
        ancla.setFecha(LocalDateTime.of(2026, 4, 24, 14, 55, 28));
        ancla.setTotal(new BigDecimal("1.50"));
        ancla.setBaseImponibleTotal(new BigDecimal("1.24"));
        ancla.setCuotaIvaTotal(new BigDecimal("0.26"));
        ancla.setTipoFactura("F2");
        ancla.setMetodoPago("EFECTIVO");
        ancla.setUsuario(admin);
        ancla.setHashVerifactu("6E2E23BC818B2217DAF31014FA6044FE4C0B6B7611A71A3BEABBE234E3BA9150");
        ancla.setHashAnterior("");
        ancla.setEstadoVerifactu("CORRECTO");
        ancla.setNumeroFacturaAnterior(null);
        ancla.setFechaAnterior(null);
        ancla.setCifEmisorAnterior(null);
        ventaRepository.save(ancla);
    }
}