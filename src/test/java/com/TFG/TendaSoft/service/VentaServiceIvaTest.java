package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.*;
import com.TFG.TendaSoft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceIvaTest {

    @Mock private VentaRepository        ventaRepository;
    @Mock private LineaVentaRepository   lineaVentaRepository;
    @Mock private ProductoRepository       productoRepository;
    @Mock private DatosNegocioRepository   datosNegocioRepository;
    @Mock private VerifactuXmlService      xmlService;
    @Mock private FirmaDigitalService      firmaDigitalService;
    @Mock private VerifactuHttpService     verifactuHttpService;

    private VentaService ventaService;

    @BeforeEach
    void setUp() {
        ventaService = new VentaService(
                ventaRepository, lineaVentaRepository, productoRepository,
                datosNegocioRepository, xmlService, firmaDigitalService, verifactuHttpService
        );
    }

    // Precio PVP 1.21€ con IVA 21% → base = 1.00€, cuota = 0.21€
    @Test
    void registrarVenta_conIva21_debeCalcularBaseEIvaCorrectamente() throws Exception {
        Producto producto = productoProductoPrueba("P001", new BigDecimal("1.21"), new BigDecimal("21.00"));
        LineaVenta linea = lineaVenta(producto, 1);
        Venta venta = ventaBase();
        DatosNegocio negocio = negocioPrueba();

        configurarMocks(producto, negocio, venta);

        Venta resultado = ventaService.registrarNuevaVenta(venta, List.of(linea));

        assertEquals(new BigDecimal("1.00"), resultado.getBaseImponibleTotal());
        assertEquals(new BigDecimal("0.21"), resultado.getCuotaIvaTotal());
        assertEquals(new BigDecimal("1.21"), resultado.getTotal());
    }

    // Precio PVP 1.10€ con IVA 10% → base = 1.00€, cuota = 0.10€
    @Test
    void registrarVenta_conIva10_debeCalcularBaseEIvaCorrectamente() throws Exception {
        Producto producto = productoProductoPrueba("P002", new BigDecimal("1.10"), new BigDecimal("10.00"));
        LineaVenta linea = lineaVenta(producto, 1);
        Venta venta = ventaBase();
        DatosNegocio negocio = negocioPrueba();

        configurarMocks(producto, negocio, venta);

        Venta resultado = ventaService.registrarNuevaVenta(venta, List.of(linea));

        assertEquals(new BigDecimal("1.00"), resultado.getBaseImponibleTotal());
        assertEquals(new BigDecimal("0.10"), resultado.getCuotaIvaTotal());
    }

    // 2 unidades a 1.21€ con IVA 21% → base = 2.00€, cuota = 0.42€, total = 2.42€
    @Test
    void registrarVenta_conVariasUnidades_debeMultiplicarCorrectamente() throws Exception {
        Producto producto = productoProductoPrueba("P003", new BigDecimal("1.21"), new BigDecimal("21.00"));
        LineaVenta linea = lineaVenta(producto, 2);
        Venta venta = ventaBase();
        DatosNegocio negocio = negocioPrueba();

        configurarMocks(producto, negocio, venta);

        Venta resultado = ventaService.registrarNuevaVenta(venta, List.of(linea));

        assertEquals(new BigDecimal("2.00"), resultado.getBaseImponibleTotal());
        assertEquals(new BigDecimal("0.42"), resultado.getCuotaIvaTotal());
        assertEquals(new BigDecimal("2.42"), resultado.getTotal());
    }

    // Mezcla de IVA 21% e IVA 10% — bases y cuotas deben sumarse correctamente
    @Test
    void registrarVenta_conMezclaDeIvas_debeAgregarCorrectamente() throws Exception {
        Producto prod21 = productoProductoPrueba("P004", new BigDecimal("1.21"), new BigDecimal("21.00"));
        Producto prod10 = productoProductoPrueba("P005", new BigDecimal("1.10"), new BigDecimal("10.00"));
        LineaVenta linea1 = lineaVenta(prod21, 1);
        LineaVenta linea2 = lineaVenta(prod10, 1);
        Venta venta = ventaBase();
        DatosNegocio negocio = negocioPrueba();

        when(datosNegocioRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(negocio));

        when(ventaRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

        when(ventaRepository.findFirstByNumeroFacturaStartingWithOrderByIdDesc(anyString())).thenReturn(Optional.empty());
        when(productoRepository.findById("P004")).thenReturn(Optional.of(prod21));
        when(productoRepository.findById("P005")).thenReturn(Optional.of(prod10));
        when(ventaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lineaVentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Venta resultado = ventaService.registrarNuevaVenta(venta, List.of(linea1, linea2));

        assertEquals(new BigDecimal("2.00"), resultado.getBaseImponibleTotal());
        assertEquals(new BigDecimal("0.31"), resultado.getCuotaIvaTotal());
        assertEquals(new BigDecimal("2.31"), resultado.getTotal());
    }

    @Test
    void registrarVenta_debeDecrementarStockDelProducto() throws Exception {
        Producto producto = productoProductoPrueba("P006", new BigDecimal("1.21"), new BigDecimal("21.00"));
        producto.setUnidades(10);
        LineaVenta linea = lineaVenta(producto, 3);
        Venta venta = ventaBase();
        DatosNegocio negocio = negocioPrueba();

        configurarMocks(producto, negocio, venta);

        ventaService.registrarNuevaVenta(venta, List.of(linea));

        // Verificamos que se guardó el producto con el stock reducido
        verify(productoRepository).save(argThat(p -> p.getUnidades() == 7));
    }

    @Test
    void registrarVenta_nula_debeLanzarExcepcion() {
        assertThrows(RuntimeException.class,
                () -> ventaService.registrarNuevaVenta(null, List.of()));
    }

    @Test
    void registrarVenta_conFacturaAnterior_debeEncadenarHuellasCriptograficas() throws Exception {
        Venta facturaPrevia = new Venta();
        facturaPrevia.setNumeroFactura("FAC-2026-0066");
        facturaPrevia.setHashVerifactu("HASH_PADRE_99998888");
        facturaPrevia.setEstadoVerifactu("ERROR_AEAT"); // Incluso con error, debe encadenar

        Producto producto = productoProductoPrueba("P001", new BigDecimal("1.21"), new BigDecimal("21.00"));
        LineaVenta linea = lineaVenta(producto, 1);
        Venta nuevaVenta = ventaBase();
        DatosNegocio negocio = negocioPrueba();

        when(datosNegocioRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(negocio));

        when(ventaRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(facturaPrevia));

        when(ventaRepository.findFirstByNumeroFacturaStartingWithOrderByIdDesc(anyString())).thenReturn(Optional.empty());
        when(productoRepository.findById(producto.getCodigoBarras())).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lineaVentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Venta resultado = ventaService.registrarNuevaVenta(nuevaVenta, List.of(linea));

        assertNotNull(resultado.getHashAnterior());
        assertEquals("HASH_PADRE_99998888", resultado.getHashAnterior());
        verify(ventaRepository).findTopByOrderByIdDesc();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Producto productoProductoPrueba(String codigo, BigDecimal precio, BigDecimal iva) {
        Producto p = new Producto();
        p.setCodigoBarras(codigo);
        p.setNombre("Producto " + codigo);
        p.setPrecio(precio);
        p.setPorcentajeIva(iva);
        p.setUnidades(100);
        return p;
    }

    private LineaVenta lineaVenta(Producto producto, int cantidad) {
        LineaVenta l = new LineaVenta();
        l.setProducto(producto);
        l.setCantidad(cantidad);
        return l;
    }

    private Venta ventaBase() {
        Venta v = new Venta();
        v.setMetodoPago("EFECTIVO");
        v.setUsuario(new Usuario());
        return v;
    }

    private DatosNegocio negocioPrueba() {
        DatosNegocio n = new DatosNegocio();
        n.setCif("A39200019");
        n.setNombreEmpresa("Test S.L.");
        n.setRutaCertificado(null); // sin certificado para tests unitarios
        return n;
    }

    private void configurarMocks(Producto producto, DatosNegocio negocio, Venta venta) {
        when(datosNegocioRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(negocio));
        when(ventaRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(ventaRepository.findFirstByNumeroFacturaStartingWithOrderByIdDesc(anyString())).thenReturn(Optional.empty());
        when(productoRepository.findById(producto.getCodigoBarras())).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lineaVentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }
}