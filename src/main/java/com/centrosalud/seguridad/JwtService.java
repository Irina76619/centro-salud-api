package com.centrosalud.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * JWT = texto firmado que el servidor entrega al hacer login. El frontend lo manda en cada peticion
 * (cabecera Authorization: Bearer ...). Como esta firmado con una clave secreta, nadie puede alterarlo.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration duracion;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long minutos) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.duracion = Duration.ofMinutes(minutos);
    }

    public String generar(Long id, String usuario, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario)
                .claim("uid", id)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracion.toMillis()))
                .signWith(key)
                .compact();
    }

    /** Valida firma y expiracion. Lanza JwtException si el token no sirve. */
    public UsuarioAutenticado leer(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Long id = ((Number) claims.get("uid")).longValue();
        return new UsuarioAutenticado(id, claims.getSubject(), claims.get("rol", String.class));
    }
}
