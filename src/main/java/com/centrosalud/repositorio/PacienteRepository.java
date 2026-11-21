package com.centrosalud.repositorio;

import com.centrosalud.modelo.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    List<Paciente> findByDni(String dni);
    List<Paciente> findByNombreIgnoreCaseAndApellidosIgnoreCase(String nombre, String apellidos);
    boolean existsByDni(String dni);
    boolean existsByDniAndIdNot(String dni, Long id);
}
