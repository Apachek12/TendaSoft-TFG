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
        if (usuario.getIdUsuario() != null) {
            throw new IllegalArgumentException("El ID debe estar vacío para crear un usuario nuevo.");
        }
        // TODO encriptación de la contraseña
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public void eliminarUsuario(Integer id) {
        usuarioRepository.deleteById(id);
    }
}