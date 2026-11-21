package com.centrosalud.controlador;

import com.centrosalud.dto.Peticiones.LoginReq;
import com.centrosalud.dto.Respuestas.LoginRes;
import com.centrosalud.servicio.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Los CONTROLADORES solo reciben la peticion HTTP, validan el JSON (@Valid) y llaman al servicio.
 * No tienen logica de negocio.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/login  { "tipo": "ADMIN" | "MEDICO", "usuario": "...", "clave": "..." } */
    @PostMapping("/login")
    public LoginRes login(@Valid @RequestBody LoginReq req) {
        return authService.login(req);
    }
}
