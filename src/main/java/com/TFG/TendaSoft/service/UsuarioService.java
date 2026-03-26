package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario crearUsuario(Usuario usuario) {
        // En tu repositorio de usuario tendrías que crear: boolean existsByNombreUsuario(String nombreUsuario);
        if (usuario.getIdUsuario() != null) {
            throw new IllegalArgumentException("El ID debe estar vacío para crear un usuario nuevo.");
        }
        // Aquí en el futuro meteremos la encriptación de la contraseña (BCrypt)
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorUsername(String username) {
        // En tu repositorio: Optional<Usuario> findByNombreUsuario(String nombreUsuario);
        return usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}