package com.centrosalud.repositorio;

import com.centrosalud.modelo.Receta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RecetaRepository extends JpaRepository<Receta, Long> {
    List<Receta> findByCitaIdOrderByIdAsc(Long citaId);
    List<Receta> findByCitaIdIn(Collection<Long> citaIds);
}
