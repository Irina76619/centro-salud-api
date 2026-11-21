package com.centrosalud.servicio;

import com.centrosalud.dto.Peticiones.CitaReq;
import com.centrosalud.dto.Respuestas.CitaRes;
import com.centrosalud.excepcion.ApiException;
import com.centrosalud.modelo.Cita;
import com.centrosalud.modelo.EstadoCita;
import com.centrosalud.modelo.Medico;
import com.centrosalud.modelo.Paciente;
import com.centrosalud.repositorio.CitaRepository;
import com.centrosalud.repositorio.HorarioRepository;
import com.centrosalud.repositorio.MedicoRepository;
import com.centrosalud.repositorio.PacienteRepository;
import com.centrosalud.util.DiasSemana;
import com.centrosalud.util.Tiempo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Gestion de citas por el administrativo, con las reglas de agenda del centro. */
@Service
@Transactional(readOnly = true)
public class CitaService {

    private final CitaRepository citas;
    private final PacienteRepository pacientes;
    private final MedicoRepository medicos;
    private final HorarioRepository horarios;

    public CitaService(CitaRepository citas, PacienteRepository pacientes,
                       MedicoRepository medicos, HorarioRepository horarios) {
        this.citas = citas;
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.horarios = horarios;
    }

    /** Todos los filtros son opcionales (null = sin filtrar). */
    public List<CitaRes> listar(Long medicoId, Long pacienteId, EstadoCita estado) {
        return citas.findAllByOrderByFechaHoraAsc().stream()
                .filter(c -> medicoId == null || c.getMedico().getId().equals(medicoId))
                .filter(c -> pacienteId == null || c.getPaciente().getId().equals(pacienteId))
                .filter(c -> estado == null || c.getEstado() == estado)
                .map(Mapeador::cita)
                .toList();
    }

    public CitaRes obtener(Long id) {
        return Mapeador.cita(buscar(id));
    }

    @Transactional
    public CitaRes crear(CitaReq req) {
        Paciente p = pacientes.findById(req.pacienteId()).orElseThrow(() -> ApiException.noEncontrado("Paciente", req.pacienteId()));
        Medico m = medicos.findById(req.medicoId()).orElseThrow(() -> ApiException.noEncontrado("Medico", req.medicoId()));
        validarAgenda(m, req.fechaHora(), 0L);
        Cita c = new Cita(req.fechaHora(), req.motivo().trim(), EstadoCita.PENDIENTE, p, m);
        return Mapeador.cita(citas.save(c));
    }

    /** Editar / reprogramar: solo citas PENDIENTES. */
    @Transactional
    public CitaRes actualizar(Long id, CitaReq req) {
        Cita c = buscar(id);
        if (c.getEstado() != EstadoCita.PENDIENTE) {
            throw ApiException.reglaNegocio("Solo se pueden modificar citas pendientes");
        }
        Paciente p = pacientes.findById(req.pacienteId()).orElseThrow(() -> ApiException.noEncontrado("Paciente", req.pacienteId()));
        Medico m = medicos.findById(req.medicoId()).orElseThrow(() -> ApiException.noEncontrado("Medico", req.medicoId()));

        boolean cambiaAgenda = !c.getFechaHora().equals(req.fechaHora()) || !c.getMedico().getId().equals(m.getId());
        if (cambiaAgenda) {
            validarAgenda(m, req.fechaHora(), c.getId());
        }
        c.setFechaHora(req.fechaHora());
        c.setMotivo(req.motivo().trim());
        c.setPaciente(p);
        c.setMedico(m);
        return Mapeador.cita(c);
    }

    @Transactional
    public CitaRes cancelar(Long id) {
        Cita c = buscar(id);
        if (c.getEstado() != EstadoCita.PENDIENTE) {
            throw ApiException.reglaNegocio("Solo se pueden cancelar citas pendientes");
        }
        c.setEstado(EstadoCita.CANCELADA);
        return Mapeador.cita(c);
    }

    @Transactional
    public void eliminar(Long id) {
        citas.delete(buscar(id));   // recetas se borran en cascada; el historial queda con cita = null
    }

    /**
     * Reglas de agenda:
     *  1) la fecha debe ser futura,
     *  2) el medico debe atender ese dia y a esa hora (segun sus Horarios),
     *  3) el medico no puede tener otra cita activa a la misma hora.
     */
    private void validarAgenda(Medico medico, LocalDateTime fechaHora, Long excluirCitaId) {
        if (fechaHora.isBefore(Tiempo.ahora())) {
            throw ApiException.reglaNegocio("La fecha y hora de la cita deben ser futuras");
        }
        String dia = DiasSemana.deFecha(fechaHora.toLocalDate());
        LocalTime hora = fechaHora.toLocalTime();
        boolean atiende = horarios.findByMedicoId(medico.getId()).stream()
                .anyMatch(h -> DiasSemana.mismoDia(h.getDiaSemana(), dia)
                        && !hora.isBefore(h.getHoraInicio()) && hora.isBefore(h.getHoraFin()));
        if (!atiende) {
            throw ApiException.reglaNegocio("El medico no atiende los " + dia + " a las " + hora + " (revisa su horario)");
        }
        if (citas.existeChoque(medico.getId(), fechaHora, EstadoCita.CANCELADA, excluirCitaId)) {
            throw ApiException.conflicto("El medico ya tiene una cita a esa fecha y hora");
        }
    }

    private Cita buscar(Long id) {
        return citas.findById(id).orElseThrow(() -> ApiException.noEncontrado("Cita", id));
    }
}
