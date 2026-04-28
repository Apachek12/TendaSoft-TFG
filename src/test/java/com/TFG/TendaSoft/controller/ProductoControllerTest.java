package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.config.SecurityConfig;
import com.TFG.TendaSoft.model.Producto;
import com.TFG.TendaSoft.repository.ProductoRepository;
import com.TFG.TendaSoft.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
class ProductoControllerTest {

    @Autowired  private MockMvc mockMvc;

    @MockitoBean private ProductoService    productoService;
    @MockitoBean private ProductoRepository productoRepository; // inyectado directamente en el controller
    @MockitoBean private PasswordEncoder    passwordEncoder;

    @Test
    void obtenerTodos_debeDevolverListaDeProductos() throws Exception {
        Producto p1 = new Producto();
        p1.setCodigoBarras("123456"); p1.setNombre("Café"); p1.setPrecio(new BigDecimal("1.50")); p1.setActivo(true);
        Producto p2 = new Producto();
        p2.setCodigoBarras("654321"); p2.setNombre("Agua"); p2.setPrecio(new BigDecimal("0.80")); p2.setActivo(true);

        when(productoService.obtenerTodosLosProductos()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Café"))
                .andExpect(jsonPath("$[1].nombre").value("Agua"));
    }

    @Test
    void obtenerTodos_sinProductos_debeDevolverListaVacia() throws Exception {
        when(productoService.obtenerTodosLosProductos()).thenReturn(List.of());

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}