package com.centrosalud.seguridad;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que se ejecuta antes de cada peticion: si trae un token valido, marca al usuario como autenticado
 * con su rol (ROLE_ADMIN / ROLE_MEDICO). Si no, la peticion sigue sin autenticar y Spring Security responde 401.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            try {
                UsuarioAutenticado usuario = jwtService.leer(cabecera.substring(7));
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (JwtException | IllegalArgumentException e) {
                // Token vencido o alterado: se ignora y la peticion queda como "no autenticada".
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
