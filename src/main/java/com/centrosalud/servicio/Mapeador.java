package com.centrosalud.servicio;

import com.centrosalud.dto.Respuestas.*;
import com.centrosalud.modelo.*;

import java.util.List;

/**
 * Convierte entidades -> DTOs. Se usa SIEMPRE dentro de un metodo @Transactional,
 * porque algunas relaciones (medico, paciente...) se cargan "perezosamente" (LAZY).
 */
public final class Mapeador {
    private Mapeador() { }

    public static EspecialidadRes especialidad(Especialidad e) {
        return new EspecialidadRes(e.getId(), e.getNombre());
    }

    public static PacienteRes paciente(Paciente p) {
        return new PacienteRes(p.getId(), p.getDni(), p.getNombre(), p.getApellidos(), p.getEdad(), p.getTelefono());
    }

    public static MedicoRes medico(Medico m) {
        return new MedicoRes(m.getId(), m.getCodigoMedico(), m.getDni(), m.getNombre(), m.getApellidos(),
                m.getEdad(), m.getEspecialidad().getId(), m.getEspecialidad().getNombre());
    }

    public static HorarioRes horario(Horario h) {
        return new HorarioRes(h.getId(), h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin(),
                h.getMedico().getId(), h.getMedico().getNombreCompleto());
    }

    public static CitaRes cita(Cita c) {
        return new CitaRes(c.getId(), c.getFechaHora(), c.getMotivo(), c.getEstado(),
                c.getPaciente().getId(), c.getPaciente().getNombreCompleto(), c.getPaciente().getDni(),
                c.getMedico().getId(), c.getMedico().getNombreCompleto(), c.getMedico().getEspecialidad().getNombre());
    }

    public static RecetaRes receta(Receta r) {
        return new RecetaRes(r.getId(), r.getMedicamento(), r.getIndicaciones(), r.getFecha(), r.getCita().getId());
    }

    public static HistorialDetalleRes historial(HistorialClinico h, List<RecetaRes> recetas) {
        Cita c = h.getCita();
        return new HistorialDetalleRes(h.getId(), h.getFecha(), h.getDiagnostico(), h.getTratamiento(),
                h.getPaciente().getId(), h.getPaciente().getNombreCompleto(),
                c == null ? null : c.getId(),
                c == null ? null : c.getMedico().getNombreCompleto(),
                recetas);
    }
}
