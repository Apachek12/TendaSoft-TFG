package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.config.SecurityConfig;
import com.TFG.TendaSoft.dto.EstadisticasDTO;
import com.TFG.TendaSoft.model.Venta;
import com.TFG.TendaSoft.service.VentaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaController.class)
@Import(SecurityConfig.class)
class VentaControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private VentaService   ventaService;
    @MockitoBean private PasswordEncoder passwordEncoder;

    @Test
    void obtenerEstadisticas_debeDevolverDatosDelPeriodo() throws Exception {
        Map<String, BigDecimal> porMetodo = new HashMap<>();
        porMetodo.put("EFECTIVO", new BigDecimal("150.00"));
        porMetodo.put("TARJETA",  new BigDecimal("80.00"));

        EstadisticasDTO dto = EstadisticasDTO.builder()
                .totalFacturado(new BigDecimal("230.00"))
                .totalTickets(5L)
                .ticketMedio(new BigDecimal("46.00"))
                .ventasPorMetodoPago(porMetodo)
                .productosVendidos(0L)
                .build();

        when(ventaService.obtenerEstadisticasDePeriodo(anyInt(), anyInt(), isNull()))
                .thenReturn(dto);

        mockMvc.perform(get("/api/ventas/estadisticas")
                        .param("año", "2026")
                        .param("mes", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFacturado").value(230.00))
                .andExpect(jsonPath("$.totalTickets").value(5));
    }

    @Test
    void registrarVenta_conPayloadValido_debeDevolver201() throws Exception {
        Venta ventaGuardada = new Venta();
        ventaGuardada.setNumeroFactura("FAC-2026-0050");
        ventaGuardada.setEstadoVerifactu("CORRECTO");

        when(ventaService.registrarNuevaVenta(any(), any())).thenReturn(ventaGuardada);

        Map<String, Object> payload = Map.of(
                "venta", Map.of("metodoPago", "EFECTIVO", "total", 1.21),
                "lineas", List.of(Map.of(
                        "producto", Map.of("codigoBarras", "123456"),
                        "cantidad", 1
                ))
        );

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroFactura").value("FAC-2026-0050"))
                .andExpect(jsonPath("$.estadoVerifactu").value("CORRECTO"));
    }
}