package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.config.SecurityConfig;
import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.repository.UsuarioRepository;
import com.TFG.TendaSoft.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class) // Carga nuestra config (permitAll + CORS) en lugar del filtro por defecto
class UsuarioControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UsuarioService    usuarioService;
    @MockitoBean private UsuarioRepository usuarioRepository;
    @MockitoBean private PasswordEncoder   passwordEncoder;

    @Test
    void login_conCredencialesCorrectas_debeDevolver200YDTOSinHash() throws Exception {
        Usuario admin = new Usuario();
        admin.setIdUsuario(1);
        admin.setNombreUsuario("admin");
        admin.setNombreReal("Administrador");
        admin.setRol("ADMIN");
        admin.setActivo(true);

        when(usuarioService.autenticar("admin", "1234")).thenReturn(admin);

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombreUsuario", "admin",
                                "contrasena", "1234"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("admin"))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                // La respuesta nunca debe contener la contraseña hasheada
                .andExpect(jsonPath("$.hashContrasena").doesNotExist());
    }

    @Test
    void login_conContrasenaIncorrecta_debeDevolver401() throws Exception {
        when(usuarioService.autenticar(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Contraseña incorrecta."));

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombreUsuario", "admin",
                                "contrasena", "wrongpassword"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_conUsuarioDeshabilitado_debeDevolver403() throws Exception {
        when(usuarioService.autenticar(anyString(), anyString()))
                .thenThrow(new IllegalStateException("El usuario está deshabilitado."));

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombreUsuario", "inactivo",
                                "contrasena", "1234"
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerTodos_debeDevolverListaDeUsuarios() throws Exception {
        Usuario u1 = new Usuario();
        u1.setIdUsuario(1); u1.setNombreUsuario("admin");    u1.setRol("ADMIN");    u1.setActivo(true);
        Usuario u2 = new Usuario();
        u2.setIdUsuario(2); u2.setNombreUsuario("vendedor"); u2.setRol("VENDEDOR"); u2.setActivo(true);

        when(usuarioService.obtenerTodos()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombreUsuario").value("admin"))
                .andExpect(jsonPath("$[1].nombreUsuario").value("vendedor"));
    }
}