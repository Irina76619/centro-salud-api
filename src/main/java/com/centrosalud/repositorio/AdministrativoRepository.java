package com.centrosalud.repositorio;

import com.centrosalud.modelo.Administrativo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministrativoRepository extends JpaRepository<Administrativo, Long> {
    Optional<Administrativo> findByUsuario(String usuario);
}
