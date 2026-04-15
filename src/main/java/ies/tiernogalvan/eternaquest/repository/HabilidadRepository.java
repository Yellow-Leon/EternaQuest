package ies.tiernogalvan.eternaquest.repository;

import ies.tiernogalvan.eternaquest.model.entity.Habilidad;
import ies.tiernogalvan.eternaquest.model.enums.ClasePersonaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HabilidadRepository extends JpaRepository<Habilidad, Long> {
    List<Habilidad> findByClaseAndNivelRequeridoLessThanEqual(ClasePersonaje clase, int nivel);
}
