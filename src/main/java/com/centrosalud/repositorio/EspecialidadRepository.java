package com.centrosalud.repositorio;

import com.centrosalud.modelo.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

/** Reemplaza a EspecialidadDAO: Spring genera el SQL a partir de los nombres de los metodos. */
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
