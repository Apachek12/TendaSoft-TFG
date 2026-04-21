package com.TFG.TendaSoft.config;

import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeedConfig {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repository) {
        return args -> {
            // Si no hay usuarios, creamos los de prueba
            if (repository.count() == 0) {
                System.out.println("Creando usuarios de prueba");

                Usuario admin = new Usuario();
                admin.setNombreUsuario("admin");
                admin.setHashContrasena("admin123");
                admin.setRol("ADMIN");

                Usuario vendedor = new Usuario();
                vendedor.setNombreUsuario("vendedor");
                vendedor.setHashContrasena("vende123");
                vendedor.setRol("VENDEDOR");

                repository.save(admin);
                repository.save(vendedor);

                System.out.println("✅ Usuarios creados: admin/admin123 y vendedor/vende123");
            }
        };
    }
}