package com.centrosalud.servicio;

import com.centrosalud.dto.Respuestas.HistorialDetalleRes;
import com.centrosalud.dto.Respuestas.RecetaRes;
import com.centrosalud.modelo.Cita;
import com.centrosalud.modelo.HistorialClinico;
import com.centrosalud.repositorio.HistorialClinicoRepository;
import com.centrosalud.repositorio.RecetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Arma el historial clinico "completo": cada registro con su medico y las recetas de su cita. */
@Service
@Transactional(readOnly = true)
public class HistorialService {

    private final HistorialClinicoRepository historiales;
    private final RecetaRepository recetas;

    public HistorialService(HistorialClinicoRepository historiales, RecetaRepository recetas) {
        this.historiales = historiales;
        this.recetas = recetas;
    }

    public List<HistorialDetalleRes> porPaciente(Long pacienteId) {
        return detalles(historiales.findByPacienteIdOrderByFechaAscIdAsc(pacienteId));
    }

    public List<HistorialDetalleRes> todos() {
        return detalles(historiales.findAllByOrderByFechaDescIdDesc());
    }

    public List<HistorialDetalleRes> detalles(List<HistorialClinico> lista) {
        List<Long> citaIds = lista.stream()
                .map(HistorialClinico::getCita)
                .filter(Objects::nonNull)
                .map(Cita::getId)
                .distinct()
                .toList();

        // Una sola consulta para todas las recetas, agrupadas por cita
        Map<Long, List<RecetaRes>> recetasPorCita = citaIds.isEmpty()
                ? Map.of()
                : recetas.findByCitaIdIn(citaIds).stream()
                    .collect(Collectors.groupingBy(r -> r.getCita().getId(),
                             Collectors.mapping(Mapeador::receta, Collectors.toList())));

        return lista.stream()
                .map(h -> Mapeador.historial(h,
                        h.getCita() == null ? List.of() : recetasPorCita.getOrDefault(h.getCita().getId(), List.of())))
                .toList();
    }
}
