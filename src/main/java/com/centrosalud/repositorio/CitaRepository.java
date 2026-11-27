package com.centrosalud.repositorio;

import com.centrosalud.modelo.Cita;
import com.centrosalud.modelo.EstadoCita;
import com.centrosalud.modelo.Paciente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @EntityGraph le dice a JPA que traiga paciente, medico y especialidad en UNA sola consulta
 * (evita hacer una consulta extra por cada cita: el problema "N+1").
 */
public interface CitaRepository extends JpaRepository<Cita, Long> {

    @EntityGraph(attributePaths = {"paciente", "medico", "medico.especialidad"})
    List<Cita> findAllByOrderByFechaHoraAsc();

    @EntityGraph(attributePaths = {"paciente", "medico", "medico.especialidad"})
    List<Cita> findByMedicoIdOrderByFechaHoraAsc(Long medicoId);

    @EntityGraph(attributePaths = {"paciente", "medico", "medico.especialidad"})
    List<Cita> findByPacienteIdOrderByFechaHoraAsc(Long pacienteId);

    /** Citas dentro de [desde, hasta): sirve para reportes por dia y por mes. */
    @EntityGraph(attributePaths = {"paciente", "medico", "medico.especialidad"})
    List<Cita> findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAsc(LocalDateTime desde, LocalDateTime hasta);

    long countByEstado(EstadoCita estado);

    long countByFechaHoraGreaterThanEqualAndFechaHoraLessThan(LocalDateTime desde, LocalDateTime hasta);

    boolean existsByMedicoIdAndPacienteId(Long medicoId, Long pacienteId);

    boolean existsByMedicoId(Long medicoId);

    boolean existsByPacienteId(Long pacienteId);

    /** Regla de negocio: un medico no puede tener 2 citas activas a la misma hora. */
    @Query("select count(c) > 0 from Cita c where c.medico.id = :medicoId and c.fechaHora = :fechaHora "
            + "and c.estado not in :excluidos and c.id <> :excluirId")
    boolean existeChoque(@Param("medicoId") Long medicoId,
                         @Param("fechaHora") LocalDateTime fechaHora,
                         @Param("excluidos") List<EstadoCita> excluidos,
                         @Param("excluirId") Long excluirId);

    /** Pacientes que tienen (o tuvieron) al menos una cita con ese medico. */
    @Query("select distinct p from Cita c join c.paciente p where c.medico.id = :medicoId")
    List<Paciente> findPacientesDeMedico(@Param("medicoId") Long medicoId);
}
