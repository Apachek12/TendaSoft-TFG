package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    // BCryptPasswordEncoder real — queremos verificar el hash de verdad
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void crearUsuario_debeHashearContrasenaConBCrypt() {
        Usuario nuevo = new Usuario();
        nuevo.setNombreUsuario("testuser");
        nuevo.setHashContrasena("1234");
        nuevo.setRol("VENDEDOR");

        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario guardado = usuarioService.crearUsuario(nuevo);

        // La contraseña guardada nunca debe ser texto plano
        assertNotEquals("1234", guardado.getHashContrasena());
        // BCrypt debe verificarla correctamente
        assertTrue(passwordEncoder.matches("1234", guardado.getHashContrasena()));
    }

    @Test
    void crearUsuario_conIdExistente_debeLanzarExcepcion() {
        Usuario nuevo = new Usuario();
        nuevo.setIdUsuario(1);
        nuevo.setHashContrasena("1234");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearUsuario(nuevo));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void autenticar_conCredencialesCorrectas_debeDevolverUsuario() {
        String hashReal = passwordEncoder.encode("1234");
        Usuario admin = new Usuario();
        admin.setNombreUsuario("admin");
        admin.setHashContrasena(hashReal);
        admin.setActivo(true);

        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(admin));

        Usuario resultado = usuarioService.autenticar("admin", "1234");
        assertEquals("admin", resultado.getNombreUsuario());
    }

    @Test
    void autenticar_conContrasenaIncorrecta_debeLanzarExcepcion() {
        String hashReal = passwordEncoder.encode("1234");
        Usuario admin = new Usuario();
        admin.setNombreUsuario("admin");
        admin.setHashContrasena(hashReal);
        admin.setActivo(true);

        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.autenticar("admin", "wrongpassword"));
    }

    @Test
    void autenticar_conUsuarioDeshabilitado_debeLanzarExcepcion() {
        String hashReal = passwordEncoder.encode("1234");
        Usuario inactivo = new Usuario();
        inactivo.setNombreUsuario("vendedor");
        inactivo.setHashContrasena(hashReal);
        inactivo.setActivo(false);

        when(usuarioRepository.findByNombreUsuario("vendedor")).thenReturn(Optional.of(inactivo));

        assertThrows(IllegalStateException.class,
                () -> usuarioService.autenticar("vendedor", "1234"));
    }

    @Test
    void autenticar_conUsuarioInexistente_debeLanzarExcepcion() {
        when(usuarioRepository.findByNombreUsuario("noexiste"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> usuarioService.autenticar("noexiste", "1234"));
    }
}