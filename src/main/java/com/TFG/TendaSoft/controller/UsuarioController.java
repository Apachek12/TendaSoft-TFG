package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.dto.UsuarioDTO;
import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.repository.UsuarioRepository;
import com.TFG.TendaSoft.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/buscar/{username}")
    public ResponseEntity<Usuario> buscarPorUsername(@PathVariable String username) {
        return ResponseEntity.ok(usuarioService.buscarPorUsername(username));
    }

    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        return new ResponseEntity<>(usuarioService.crearUsuario(usuario), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Integer id, @RequestBody Usuario datosActualizados) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombreReal(datosActualizados.getNombreReal());
            usuario.setNombreUsuario(datosActualizados.getNombreUsuario());
            usuario.setRol(datosActualizados.getRol());

            // Solo actualizamos la contraseña si se envía una nueva (y la hasheamos)
            if (datosActualizados.getHashContrasena() != null && !datosActualizados.getHashContrasena().isEmpty()) {
                usuario.setHashContrasena(passwordEncoder.encode(datosActualizados.getHashContrasena()));
            }

            return ResponseEntity.ok(usuarioRepository.save(usuario));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Soft delete: alterna el estado activo/inactivo del usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<?> toggleEstadoUsuario(@PathVariable Integer id) {
        return usuarioRepository.findById(id).map(usuario -> {
            boolean estadoActual = usuario.getActivo() != null ? usuario.getActivo() : true;
            usuario.setActivo(!estadoActual);
            usuarioRepository.save(usuario);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // Devuelve un DTO sin hashContrasena para no exponer datos sensibles al frontend
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String username = credenciales.get("nombreUsuario");
        String password = credenciales.get("contrasena");

        try {
            Usuario usuario = usuarioService.autenticar(username, password);
            return ResponseEntity.ok(toDTO(usuario));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario no existe");
        }
    }

    private UsuarioDTO toDTO(Usuario u) {
        return UsuarioDTO.builder()
                .idUsuario(u.getIdUsuario())
                .nombreReal(u.getNombreReal())
                .nombreUsuario(u.getNombreUsuario())
                .rol(u.getRol())
                .activo(u.getActivo())
                .build();
    }
}