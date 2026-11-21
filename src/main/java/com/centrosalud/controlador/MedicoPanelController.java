package com.centrosalud.controlador;

import com.centrosalud.dto.Peticiones.AtenderReq;
import com.centrosalud.dto.Peticiones.HistorialReq;
import com.centrosalud.dto.Peticiones.RecetaReq;
import com.centrosalud.dto.Respuestas.*;
import com.centrosalud.modelo.EstadoCita;
import com.centrosalud.seguridad.UsuarioAutenticado;
import com.centrosalud.servicio.AtencionService;
import com.centrosalud.servicio.MedicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Panel del medico. @AuthenticationPrincipal entrega el usuario sacado del token:
 * el id del medico NUNCA viene del frontend, asi nadie puede pedir datos de otro medico.
 */
@RestController
@RequestMapping("/api/medico")
public class MedicoPanelController {

    private final AtencionService atencion;
    private final MedicoService medicos;

    public MedicoPanelController(AtencionService atencion, MedicoService medicos) {
        this.atencion = atencion;
        this.medicos = medicos;
    }

    @GetMapping("/perfil")
    public MedicoRes perfil(@AuthenticationPrincipal UsuarioAutenticado u) {
        return medicos.obtener(u.id());
    }

    /** GET /api/medico/citas?estado=PENDIENTE (estado opcional) */
    @GetMapping("/citas")
    public List<CitaRes> misCitas(@AuthenticationPrincipal UsuarioAutenticado u,
                                  @RequestParam(required = false) EstadoCita estado) {
        return atencion.misCitas(u.id(), estado);
    }

    @GetMapping("/citas/hoy")
    public List<CitaRes> citasDeHoy(@AuthenticationPrincipal UsuarioAutenticado u) {
        return atencion.citasDeHoy(u.id());
    }

    @GetMapping("/horario")
    public List<HorarioRes> miHorario(@AuthenticationPrincipal UsuarioAutenticado u) {
        return atencion.miHorario(u.id());
    }

    @GetMapping("/pacientes")
    public List<PacienteRes> misPacientes(@AuthenticationPrincipal UsuarioAutenticado u) {
        return atencion.misPacientes(u.id());
    }

    @GetMapping("/pacientes/{pacienteId}/historial")
    public List<HistorialDetalleRes> historial(@AuthenticationPrincipal UsuarioAutenticado u,
                                               @PathVariable Long pacienteId) {
        return atencion.historialDePaciente(u.id(), pacienteId);
    }

    /** Cierra la cita: guarda diagnostico, tratamiento y recetas, y la marca como ATENDIDA. */
    @PostMapping("/citas/{citaId}/atender")
    public CitaRes atender(@AuthenticationPrincipal UsuarioAutenticado u,
                           @PathVariable Long citaId, @Valid @RequestBody AtenderReq req) {
        return atencion.atender(u.id(), citaId, req);
    }

    @PutMapping("/historial/{historialId}")
    public HistorialDetalleRes editarHistorial(@AuthenticationPrincipal UsuarioAutenticado u,
                                               @PathVariable Long historialId, @Valid @RequestBody HistorialReq req) {
        return atencion.editarHistorial(u.id(), historialId, req);
    }

    @GetMapping("/citas/{citaId}/recetas")
    public List<RecetaRes> recetas(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable Long citaId) {
        return atencion.recetasDeCita(u.id(), citaId);
    }

    @PostMapping("/citas/{citaId}/recetas")
    @ResponseStatus(HttpStatus.CREATED)
    public RecetaRes agregarReceta(@AuthenticationPrincipal UsuarioAutenticado u,
                                   @PathVariable Long citaId, @Valid @RequestBody RecetaReq req) {
        return atencion.agregarReceta(u.id(), citaId, req);
    }

    @PutMapping("/recetas/{recetaId}")
    public RecetaRes editarReceta(@AuthenticationPrincipal UsuarioAutenticado u,
                                  @PathVariable Long recetaId, @Valid @RequestBody RecetaReq req) {
        return atencion.editarReceta(u.id(), recetaId, req);
    }

    @DeleteMapping("/recetas/{recetaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarReceta(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable Long recetaId) {
        atencion.eliminarReceta(u.id(), recetaId);
    }
}
