package com.centrosalud.servicio;

import com.centrosalud.dto.Peticiones.LoginReq;
import com.centrosalud.dto.Respuestas.LoginRes;
import com.centrosalud.excepcion.ApiException;
import com.centrosalud.modelo.Administrativo;
import com.centrosalud.modelo.Medico;
import com.centrosalud.modelo.Persona;
import com.centrosalud.repositorio.AdministrativoRepository;
import com.centrosalud.repositorio.MedicoRepository;
import com.centrosalud.seguridad.IntentosLoginService;
import com.centrosalud.seguridad.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Equivale a ServicioLogin de la version consola, pero con BCrypt, JWT y bloqueo por intentos. */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AdministrativoRepository administrativos;
    private final MedicoRepository medicos;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final IntentosLoginService intentos;

    public AuthService(AdministrativoRepository administrativos, MedicoRepository medicos,
                       PasswordEncoder encoder, JwtService jwt, IntentosLoginService intentos) {
        this.administrativos = administrativos;
        this.medicos = medicos;
        this.encoder = encoder;
        this.jwt = jwt;
        this.intentos = intentos;
    }

    public LoginRes login(LoginReq req) {
        String tipo = req.tipo().trim().toUpperCase();
        String usuario = req.usuario().trim();
        String claveIntentos = tipo + ":" + usuario.toLowerCase();

        intentos.verificarNoBloqueado(claveIntentos);

        Persona persona = null;   // POLIMORFISMO: Administrativo y Medico se tratan como Persona
        String hash = null;
        if (tipo.equals("ADMIN")) {
            Administrativo a = administrativos.findByUsuario(usuario).orElse(null);
            if (a != null) { persona = a; hash = a.getClave(); }
        } else if (tipo.equals("MEDICO")) {
            Medico m = medicos.findByCodigoMedico(usuario).orElse(null);
            if (m != null) { persona = m; hash = m.getClave(); }
        } else {
            throw ApiException.reglaNegocio("El tipo debe ser ADMIN o MEDICO");
        }

        // Mismo mensaje si el usuario no existe o la clave es mala (no revela cual fallo)
        if (persona == null || !encoder.matches(req.clave(), hash)) {
            int restantes = intentos.registrarFallo(claveIntentos);
            if (restantes <= 0) {
                throw ApiException.demasiadosIntentos("Demasiados intentos fallidos. Acceso bloqueado por 5 minutos.");
            }
            throw ApiException.noAutorizado("Credenciales incorrectas. Intentos restantes: " + restantes);
        }

        intentos.limpiar(claveIntentos);
        String token = jwt.generar(persona.getId(), usuario, tipo);
        return new LoginRes(token, tipo, persona.getId(), persona.getNombreCompleto(), persona.descripcion());
    }
}
