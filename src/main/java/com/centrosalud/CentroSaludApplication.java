package com.centrosalud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Punto de entrada (equivale al Main de la version consola).
 * Se excluye UserDetailsServiceAutoConfiguration porque el login lo hacemos nosotros
 * (AuthService + JWT) y no queremos el usuario "user" con clave aleatoria de Spring.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CentroSaludApplication {
    public static void main(String[] args) {
        SpringApplication.run(CentroSaludApplication.class, args);
    }
}
