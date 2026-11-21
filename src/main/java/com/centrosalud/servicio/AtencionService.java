package com.centrosalud.servicio;

import com.centrosalud.dto.Peticiones.AtenderReq;
import com.centrosalud.dto.Peticiones.HistorialReq;
import com.centrosalud.dto.Peticiones.RecetaReq;
import com.centrosalud.dto.Respuestas.*;
import com.centrosalud.excepcion.ApiException;
import com.centrosalud.modelo.*;
import com.centrosalud.repositorio.*;
import com.centrosalud.util.Tiempo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Panel del MEDICO. Todos los metodos reciben "medicoId" tomado del TOKEN (no del frontend),
 * y verifican que la cita/paciente/registro pertenezca a ese medico: asi un medico jamas ve datos ajenos.
 */
@Service
@Transactional(readOnly = true)
public class AtencionService {

    private final CitaRepository citas;
    private final HorarioRepository horarios;
    private final HistorialClinicoRepository historiales;
    private final RecetaRepository recetas;
    private final HistorialService historialService;

    public AtencionService(CitaRepository citas, HorarioRepository horarios,
                           HistorialClinicoRepository historiales, RecetaRepository recetas,
                           HistorialService historialService) {
        this.citas = citas;
        this.horarios = horarios;
        this.historiales = historiales;
        this.recetas = recetas;
        this.historialService = historialService;
    }

    public List<CitaRes> misCitas(Long medicoId, EstadoCita estado) {
        return citas.findByMedicoIdOrderByFechaHoraAsc(medicoId).stream()
                .filter(c -> estado == null || c.getEstado() == estado)
                .map(Mapeador::cita)
                .toList();
    }

    public List<CitaRes> citasDeHoy(Long medicoId) {
        LocalDate hoy = Tiempo.hoy();
        return citas.findByMedicoIdOrderByFechaHoraAsc(medicoId).stream()
                .filter(c -> c.getFechaHora().toLocalDate().equals(hoy))
                .map(Mapeador::cita)
                .toList();
    }

    public List<HorarioRes> miHorario(Long medicoId) {
        return horarios.findByMedicoId(medicoId).stream()
                .sorted(Comparator.comparingInt((Horario h) -> com.centrosalud.util.DiasSemana.indice(h.getDiaSemana()))
                        .thenComparing(Horario::getHoraInicio))
                .map(Mapeador::horario)
                .toList();
    }

    /** Solo los pacientes que tienen o tuvieron una cita con este medico. */
    public List<PacienteRes> misPacientes(Long medicoId) {
        return citas.findPacientesDeMedico(medicoId).stream()
                .sorted(Comparator.comparing(Paciente::getApellidos).thenComparing(Paciente::getNombre))
                .map(Mapeador::paciente)
                .toList();
    }

    public List<HistorialDetalleRes> historialDePaciente(Long medicoId, Long pacienteId) {
        if (!citas.existsByMedicoIdAndPacienteId(medicoId, pacienteId)) {
            throw ApiException.prohibido("Este paciente no tiene citas contigo");
        }
        return historialService.porPaciente(pacienteId);
    }

    /** "Editar la cita activa y cerrarla": crea el historial, las recetas y marca la cita como ATENDIDA. */
    @Transactional
    public CitaRes atender(Long medicoId, Long citaId, AtenderReq req) {
        Cita cita = citaPropia(medicoId, citaId);
        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw ApiException.reglaNegocio("Solo se puede atender una cita pendiente");
        }
        LocalDate hoy = Tiempo.hoy();
        historiales.save(new HistorialClinico(req.diagnostico().trim(), req.tratamiento().trim(), hoy, cita.getPaciente(), cita));
        if (req.recetas() != null) {
            for (RecetaReq r : req.recetas()) {
                recetas.save(new Receta(r.medicamento().trim(), r.indicaciones().trim(), hoy, cita));
            }
        }
        cita.setEstado(EstadoCita.ATENDIDA);
        return Mapeador.cita(cita);
    }

    /** Solo se pueden editar registros de historial generados en citas de este medico. */
    @Transactional
    public HistorialDetalleRes editarHistorial(Long medicoId, Long historialId, HistorialReq req) {
        HistorialClinico h = historiales.findById(historialId)
                .orElseThrow(() -> ApiException.noEncontrado("Historial", historialId));
        if (h.getCita() == null || !h.getCita().getMedico().getId().equals(medicoId)) {
            throw ApiException.prohibido("Solo puedes editar registros de tus propias citas");
        }
        h.setDiagnostico(req.diagnostico().trim());
        h.setTratamiento(req.tratamiento().trim());
        return historialService.detalles(List.of(h)).get(0);
    }

    public List<RecetaRes> recetasDeCita(Long medicoId, Long citaId) {
        citaPropia(medicoId, citaId);
        return recetas.findByCitaIdOrderByIdAsc(citaId).stream().map(Mapeador::receta).toList();
    }

    @Transactional
    public RecetaRes agregarReceta(Long medicoId, Long citaId, RecetaReq req) {
        Cita cita = citaPropia(medicoId, citaId);
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw ApiException.reglaNegocio("No se pueden agregar recetas a una cita cancelada");
        }
        Receta r = recetas.save(new Receta(req.medicamento().trim(), req.indicaciones().trim(), Tiempo.hoy(), cita));
        return Mapeador.receta(r);
    }

    @Transactional
    public RecetaRes editarReceta(Long medicoId, Long recetaId, RecetaReq req) {
        Receta r = recetaPropia(medicoId, recetaId);
        r.setMedicamento(req.medicamento().trim());
        r.setIndicaciones(req.indicaciones().trim());
        return Mapeador.receta(r);
    }

    @Transactional
    public void eliminarReceta(Long medicoId, Long recetaId) {
        recetas.delete(recetaPropia(medicoId, recetaId));
    }

    // ---------- verificaciones de pertenencia ----------

    private Cita citaPropia(Long medicoId, Long citaId) {
        Cita c = citas.findById(citaId).orElseThrow(() -> ApiException.noEncontrado("Cita", citaId));
        if (!c.getMedico().getId().equals(medicoId)) {
            throw ApiException.prohibido("Esa cita no pertenece a este medico");
        }
        return c;
    }

    private Receta recetaPropia(Long medicoId, Long recetaId) {
        Receta r = recetas.findById(recetaId).orElseThrow(() -> ApiException.noEncontrado("Receta", recetaId));
        if (!r.getCita().getMedico().getId().equals(medicoId)) {
            throw ApiException.prohibido("Esa receta no pertenece a una cita de este medico");
        }
        return r;
    }
}
