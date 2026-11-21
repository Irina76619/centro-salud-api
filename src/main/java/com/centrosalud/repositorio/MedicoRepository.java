package com.centrosalud.repositorio;

import com.centrosalud.modelo.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Optional<Medico> findByCodigoMedico(String codigoMedico);
    boolean existsByCodigoMedico(String codigoMedico);
    boolean existsByCodigoMedicoAndIdNot(String codigoMedico, Long id);
    boolean existsByDni(String dni);
    boolean existsByDniAndIdNot(String dni, Long id);
    boolean existsByEspecialidadId(Long especialidadId);
}
