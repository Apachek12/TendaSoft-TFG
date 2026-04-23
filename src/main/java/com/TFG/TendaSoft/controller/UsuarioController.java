package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.repository.UsuarioRepository;
import com.TFG.TendaSoft.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

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

    // --- ACTUALIZAR USUARIO ---
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Integer id, @RequestBody Usuario datosActualizados) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombreReal(datosActualizados.getNombreReal());
            usuario.setNombreUsuario(datosActualizados.getNombreUsuario());
            usuario.setRol(datosActualizados.getRol());

            // Solo actualizamos contraseña si se envía una nueva
            if (datosActualizados.getHashContrasena() != null && !datosActualizados.getHashContrasena().isEmpty()) {
                usuario.setHashContrasena(datosActualizados.getHashContrasena());
            }

            usuarioRepository.save(usuario);
            return ResponseEntity.ok(usuario);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> toggleEstadoUsuario(@PathVariable Integer id) {
        return usuarioRepository.findById(id).map(usuario -> {
            // Leemos el estado actual (si es null por algún motivo, asumimos que era true)
            boolean estadoActual = usuario.getActivo() != null ? usuario.getActivo() : true;

            // Lo invertimos: si era true pasa a false, y si era false pasa a true
            usuario.setActivo(!estadoActual);

            usuarioRepository.save(usuario);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody java.util.Map<String, String> credenciales) {
        String username = credenciales.get("nombreUsuario");
        String password = credenciales.get("contrasena");

        try {
            // Buscamos el usuario en la base de datos
            Usuario usuario = usuarioService.buscarPorUsername(username);

            // TODO encriptar
            if (usuario != null && usuario.getActivo() == true && usuario.getHashContrasena().equals(password)) {
                // Login correcto: devolvemos el usuario completo (incluyendo su rol ADMIN o VENDEDOR)
                return ResponseEntity.ok(usuario);
            } else {
                // Login incorrecto: error 401
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Contraseña incorrecta");
            }
        } catch (Exception e) {
            // Si el buscarPorUsername lanza un error porque no lo encuentra: error 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario no existe");
        }
    }
}