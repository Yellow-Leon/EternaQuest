package ies.tiernogalvan.eternaquest.repository;

import ies.tiernogalvan.eternaquest.model.entity.Combate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CombateRepository extends JpaRepository<Combate, Long> {
    List<Combate> findByPersonajeIdOrderByFechaDesc(Long personajeId, Pageable pageable);
}
