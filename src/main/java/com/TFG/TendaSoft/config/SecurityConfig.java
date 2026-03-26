package com.TFG.TendaSoft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Desactivamos la protección CSRF.
                // Esto es obligatorio desactivarlo cuando hacemos una API REST para React o Postman,
                // de lo contrario bloqueará todos los POST, PUT y DELETE.
                .csrf(csrf -> csrf.disable())

                // 2. Le decimos que, por ahora, permita el paso a cualquier URL sin pedir contraseña.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}