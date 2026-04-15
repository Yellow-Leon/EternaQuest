package ies.tiernogalvan.eternaquest.repository;

import ies.tiernogalvan.eternaquest.model.entity.InventarioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventarioItemRepository extends JpaRepository<InventarioItem, Long> {
    List<InventarioItem> findByPersonajeId(Long personajeId);
    Optional<InventarioItem> findByPersonajeIdAndObjetoId(Long personajeId, Long objetoId);
    List<InventarioItem> findByPersonajeIdAndEquipadoTrue(Long personajeId);
}
