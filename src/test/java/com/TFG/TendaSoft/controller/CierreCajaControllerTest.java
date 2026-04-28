package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.config.SecurityConfig;
import com.TFG.TendaSoft.model.CierreCaja;
import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.service.CierreCajaService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CierreCajaController.class)
@Import(SecurityConfig.class)
class CierreCajaControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CierreCajaService cierreCajaService;
    @MockitoBean private PasswordEncoder   passwordEncoder;

    @Test
    void abrirCaja_conFondoValido_debeDevolver201() throws Exception {
        CierreCaja caja = new CierreCaja();
        caja.setFondoInicial(new BigDecimal("100.00"));

        when(cierreCajaService.abrirCaja(any(BigDecimal.class), any(Usuario.class)))
                .thenReturn(caja);

        mockMvc.perform(post("/api/caja/abrir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fondoInicial", 100.00,
                                "usuario", Map.of("idUsuario", 1)
                        ))))
                .andExpect(status().isCreated());
    }

    @Test
    void estadoCaja_conUsuarioSinCajaAbierta_debeDevolver200() throws Exception {
        when(cierreCajaService.obtenerCajaAbierta(1)).thenReturn(null);

        mockMvc.perform(get("/api/caja/estado/1"))
                .andExpect(status().isOk());
    }

    @Test
    void estadoCaja_conCajaAbierta_debeDevolverCaja() throws Exception {
        CierreCaja caja = new CierreCaja();
        caja.setFondoInicial(new BigDecimal("50.00"));

        when(cierreCajaService.obtenerCajaAbierta(1)).thenReturn(caja);

        mockMvc.perform(get("/api/caja/estado/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fondoInicial").value(50.00));
    }

    @Test
    void cerrarCaja_conDatosValidos_debeDevolver200() throws Exception {
        CierreCaja cajaCerrada = new CierreCaja();
        cajaCerrada.setFondoFinal(new BigDecimal("95.00"));
        cajaCerrada.setDescuadre(new BigDecimal("-5.00"));

        when(cierreCajaService.cerrarCaja(eq(1), any(BigDecimal.class)))
                .thenReturn(cajaCerrada);

        mockMvc.perform(post("/api/caja/cerrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idUsuario", 1,
                                "dineroFisicoContado", 95.00
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descuadre").value(-5.00));
    }
}