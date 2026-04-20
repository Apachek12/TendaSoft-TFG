package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.model.Usuario;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody java.util.Map<String, String> credenciales) {
        String username = credenciales.get("nombreUsuario");
        String password = credenciales.get("contrasena");

        try {
            // Buscamos el usuario en la base de datos
            Usuario usuario = usuarioService.buscarPorUsername(username);

            // Comparamos contraseñas (Ojo: para un TFG de 10, la contraseña debería estar encriptada con BCrypt,
            // pero para arrancar y probar que la conexión funciona, la comparamos tal cual)
            if (usuario != null && usuario.getHashContrasena().equals(password)) {
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