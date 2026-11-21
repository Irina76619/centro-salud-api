package com.centrosalud.repositorio;

import com.centrosalud.modelo.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByMedicoId(Long medicoId);
}
