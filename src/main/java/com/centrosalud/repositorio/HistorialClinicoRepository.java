package com.centrosalud.repositorio;

import com.centrosalud.modelo.HistorialClinico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long> {

    @EntityGraph(attributePaths = {"paciente", "cita", "cita.medico"})
    List<HistorialClinico> findByPacienteIdOrderByFechaAscIdAsc(Long pacienteId);

    @EntityGraph(attributePaths = {"paciente", "cita", "cita.medico"})
    List<HistorialClinico> findAllByOrderByFechaDescIdDesc();

    boolean existsByCitaId(Long citaId);
}
