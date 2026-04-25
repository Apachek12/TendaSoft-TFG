package com.TFG.TendaSoft.config;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// Puente entre application.properties y el converter JPA Encriptado.
// Como los @Converter JPA no son beans de Spring, no pueden usar @Value directamente.
// Esta clase sí es un bean, lee las propiedades y las expone de forma estática
// para que Encriptado.java pueda acceder a ellas en tiempo de conversión.
@Component
@ConfigurationProperties(prefix = "app.encriptado")
@Setter
public class EncriptadoConfig {

    private String clave;
    private String salt;

    // Valores estáticos que el converter JPA puede leer sin necesitar inyección
    private static String claveEstatica;
    private static String saltEstatica;

    // PostConstruct garantiza que Spring ha inyectado los valores antes de exponerlos
    @PostConstruct
    public void init() {
        claveEstatica = this.clave;
        saltEstatica  = this.salt;
    }

    public static String getClave() { return claveEstatica; }
    public static String getSalt()  { return saltEstatica; }
}