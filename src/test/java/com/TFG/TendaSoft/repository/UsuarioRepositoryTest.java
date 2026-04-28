package com.TFG.TendaSoft.repository;

import com.TFG.TendaSoft.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        Usuario admin = new Usuario();
        admin.setNombreUsuario("admin");
        admin.setNombreReal("Administrador");
        admin.setHashContrasena("$2a$10$hashadmin");
        admin.setRol("ADMIN");
        admin.setActivo(true);
        usuarioRepository.save(admin);

        Usuario vendedor = new Usuario();
        vendedor.setNombreUsuario("vendedor1");
        vendedor.setNombreReal("Juan García");
        vendedor.setHashContrasena("$2a$10$hashvendedor");
        vendedor.setRol("VENDEDOR");
        vendedor.setActivo(true);
        usuarioRepository.save(vendedor);

        Usuario inactivo = new Usuario();
        inactivo.setNombreUsuario("exvendedor");
        inactivo.setNombreReal("Pedro López");
        inactivo.setHashContrasena("$2a$10$hashinactivo");
        inactivo.setRol("VENDEDOR");
        inactivo.setActivo(false);
        usuarioRepository.save(inactivo);
    }

    @Test
    void findByNombreUsuario_conUsuarioExistente_debeEncontrarle() {
        Optional<Usuario> resultado = usuarioRepository.findByNombreUsuario("admin");
        assertTrue(resultado.isPresent());
        assertEquals("Administrador", resultado.get().getNombreReal());
    }

    @Test
    void findByNombreUsuario_conUsuarioInexistente_debeDevolverVacio() {
        Optional<Usuario> resultado = usuarioRepository.findByNombreUsuario("noexiste");
        assertFalse(resultado.isPresent());
    }

    @Test
    void findAll_debeDevolverTodosLosUsuariosIncluidosInactivos() {
        List<Usuario> todos = usuarioRepository.findAll();
        assertEquals(3, todos.size());
    }

    @Test
    void toggleActivo_debeGuardarCambioCorrectamente() {
        Usuario usuario = usuarioRepository.findByNombreUsuario("vendedor1").orElseThrow();
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        Usuario actualizado = usuarioRepository.findByNombreUsuario("vendedor1").orElseThrow();
        assertFalse(actualizado.getActivo());
    }
}