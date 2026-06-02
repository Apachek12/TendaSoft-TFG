package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.builder()
                .username(usuario.getNombreUsuario())
                .password(usuario.getHashContrasena())
                .authorities(usuario.getRol())
                .build();
    }
}