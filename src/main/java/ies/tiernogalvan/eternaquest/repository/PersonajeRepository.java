package ies.tiernogalvan.eternaquest.repository;

import ies.tiernogalvan.eternaquest.model.entity.Personaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PersonajeRepository extends JpaRepository<Personaje, Long> {
    Optional<Personaje> findByUsuarioId(Long usuarioId);

    @Query("SELECT p FROM Personaje p WHERE p.zonaActual.id = :zonaId AND p.id != :miPersonajeId")
    List<Personaje> findByZonaActualIdAndIdNot(Long zonaId, Long miPersonajeId);
}
