package com.centrosalud.servicio;

import com.centrosalud.dto.Peticiones.HorarioReq;
import com.centrosalud.dto.Respuestas.HorarioRes;
import com.centrosalud.excepcion.ApiException;
import com.centrosalud.modelo.Horario;
import com.centrosalud.modelo.Medico;
import com.centrosalud.repositorio.HorarioRepository;
import com.centrosalud.repositorio.MedicoRepository;
import com.centrosalud.util.DiasSemana;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HorarioService {

    private final HorarioRepository horarios;
    private final MedicoRepository medicos;

    public HorarioService(HorarioRepository horarios, MedicoRepository medicos) {
        this.horarios = horarios;
        this.medicos = medicos;
    }

    /** Lista todos los horarios, o solo los de un medico si se pasa medicoId. Ordenados por medico, dia y hora. */
    public List<HorarioRes> listar(Long medicoId) {
        List<Horario> lista = (medicoId == null) ? horarios.findAll() : horarios.findByMedicoId(medicoId);
        return lista.stream()
                .sorted(Comparator.comparing((Horario h) -> h.getMedico().getId())
                        .thenComparingInt(h -> DiasSemana.indice(h.getDiaSemana()))
                        .thenComparing(Horario::getHoraInicio))
                .map(Mapeador::horario)
                .toList();
    }

    public HorarioRes obtener(Long id) {
        return Mapeador.horario(buscar(id));
    }

    @Transactional
    public HorarioRes crear(HorarioReq req) {
        Medico m = medicos.findById(req.medicoId()).orElseThrow(() -> ApiException.noEncontrado("Medico", req.medicoId()));
        String dia = validar(req, m.getId(), 0L);
        return Mapeador.horario(horarios.save(new Horario(dia, req.horaInicio(), req.horaFin(), m)));
    }

    @Transactional
    public HorarioRes actualizar(Long id, HorarioReq req) {
        Horario h = buscar(id);
        Medico m = medicos.findById(req.medicoId()).orElseThrow(() -> ApiException.noEncontrado("Medico", req.medicoId()));
        String dia = validar(req, m.getId(), id);
        h.setDiaSemana(dia);
        h.setHoraInicio(req.horaInicio());
        h.setHoraFin(req.horaFin());
        h.setMedico(m);
        return Mapeador.horario(h);
    }

    @Transactional
    public void eliminar(Long id) {
        horarios.delete(buscar(id));
    }

    /** Valida dia, rango de horas y que no se cruce con otro bloque del mismo medico. Devuelve el dia canonico. */
    private String validar(HorarioReq req, Long medicoId, Long excluirId) {
        String dia = DiasSemana.canonico(req.diaSemana());
        if (dia == null) {
            throw ApiException.reglaNegocio("Dia invalido. Usa Lunes, Martes, Miércoles, Jueves, Viernes, Sábado o Domingo");
        }
        if (!req.horaFin().isAfter(req.horaInicio())) {
            throw ApiException.reglaNegocio("La hora de fin debe ser posterior a la hora de inicio");
        }
        boolean cruce = horarios.findByMedicoId(medicoId).stream()
                .filter(h -> !h.getId().equals(excluirId))
                .filter(h -> DiasSemana.mismoDia(h.getDiaSemana(), dia))
                .anyMatch(h -> req.horaInicio().isBefore(h.getHoraFin()) && req.horaFin().isAfter(h.getHoraInicio()));
        if (cruce) {
            throw ApiException.conflicto("El horario se cruza con otro bloque del mismo medico ese dia");
        }
        return dia;
    }

    private Horario buscar(Long id) {
        return horarios.findById(id).orElseThrow(() -> ApiException.noEncontrado("Horario", id));
    }
}
