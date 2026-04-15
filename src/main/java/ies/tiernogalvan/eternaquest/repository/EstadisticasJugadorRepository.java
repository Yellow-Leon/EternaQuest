package ies.tiernogalvan.eternaquest.repository;

import ies.tiernogalvan.eternaquest.model.entity.EstadisticasJugador;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EstadisticasJugadorRepository extends JpaRepository<EstadisticasJugador, Long> {
    Optional<EstadisticasJugador> findByPersonajeId(Long personajeId);
    List<EstadisticasJugador> findAllByOrderByPuntosRankingDesc(Pageable pageable);
}
