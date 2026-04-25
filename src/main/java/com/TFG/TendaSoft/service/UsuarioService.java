package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    public Usuario crearUsuario(Usuario usuario) {
        if (usuario.getIdUsuario() != null) {
            throw new IllegalArgumentException("El ID debe estar vacío al crear un usuario nuevo.");
        }
        usuario.setHashContrasena(passwordEncoder.encode(usuario.getHashContrasena()));
        return usuarioRepository.save(usuario);
    }

    // Comprueba credenciales devolviendo el usuario si son correctas, o lanzando excepción si no.
    // El controller delega aquí para no exponer lógica de autenticación en la capa HTTP.
    public Usuario autenticar(String username, String password) {
        Usuario usuario = buscarPorUsername(username);

        if (!usuario.getActivo()) {
            throw new IllegalStateException("El usuario está deshabilitado.");
        }
        if (!passwordEncoder.matches(password, usuario.getHashContrasena())) {
            throw new IllegalArgumentException("Contraseña incorrecta.");
        }
        return usuario;
    }
}