package com.TFG.TendaSoft.repository;

import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.model.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class VentaRepositoryTest {

    @Autowired private VentaRepository    ventaRepository;
    @Autowired private UsuarioRepository  usuarioRepository;

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        Usuario u = new Usuario();
        u.setNombreUsuario("admin");
        u.setNombreReal("Admin");
        u.setHashContrasena("hash");
        u.setRol("ADMIN");
        u.setActivo(true);
        usuarioPrueba = usuarioRepository.save(u);

        guardarVenta("FAC-2026-0001", "CORRECTO",  LocalDateTime.of(2026, 1, 10, 10, 0), "ABC1");
        guardarVenta("FAC-2026-0002", "CORRECTO",  LocalDateTime.of(2026, 1, 15, 10, 0), "ABC2");
        guardarVenta("FAC-2026-0003", "ERROR_AEAT", LocalDateTime.of(2026, 2, 5,  10, 0), "ABC3");
        guardarVenta("FAC-2026-0004", "FIRMADO",   LocalDateTime.of(2026, 2, 20, 10, 0), "ABC4");
    }

    @Test
    void findFirstByEstadoVerifactuOrderByIdDesc_debeDevolverUltimaCorrecta() {
        Optional<Venta> ultima = ventaRepository.findFirstByEstadoVerifactuOrderByIdDesc("CORRECTO");
        assertTrue(ultima.isPresent());
        assertEquals("FAC-2026-0002", ultima.get().getNumeroFactura());
    }

    @Test
    void findFirstByNumeroFacturaStartingWith_debeDevolverUltimaDelAño() {
        Optional<Venta> ultima = ventaRepository
                .findFirstByNumeroFacturaStartingWithOrderByIdDesc("FAC-2026");
        assertTrue(ultima.isPresent());
        assertEquals("FAC-2026-0004", ultima.get().getNumeroFactura());
    }

    @Test
    void findByFechaBetween_debeDevolverVentasDelPeriodo() {
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime fin    = LocalDateTime.of(2026, 1, 31, 23, 59);

        List<Venta> enero = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
        assertEquals(2, enero.size());
    }

    @Test
    void findByFechaBetween_fueraDePeriodo_debeDevolverListaVacia() {
        LocalDateTime inicio = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime fin    = LocalDateTime.of(2025, 12, 31, 23, 59);

        List<Venta> ventas = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
        assertTrue(ventas.isEmpty());
    }

    @Test
    void findFirstByNumeroFacturaStartingWith_sinVentasDelAño_debeDevolverVacio() {
        Optional<Venta> resultado = ventaRepository
                .findFirstByNumeroFacturaStartingWithOrderByIdDesc("FAC-2025");
        assertFalse(resultado.isPresent());
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private void guardarVenta(String numero, String estado, LocalDateTime fecha, String hash) {
        Venta v = new Venta();
        v.setNumeroFactura(numero);
        v.setEstadoVerifactu(estado);
        v.setFecha(fecha);
        v.setTotal(new BigDecimal("10.00"));
        v.setBaseImponibleTotal(new BigDecimal("8.26"));
        v.setCuotaIvaTotal(new BigDecimal("1.74"));
        v.setTipoFactura("F2");
        v.setMetodoPago("EFECTIVO");
        v.setHashVerifactu(hash);
        v.setHashAnterior("");
        v.setUsuario(usuarioPrueba);
        ventaRepository.save(v);
    }
}