package ies.tiernogalvan.eternaquest.repository;

import ies.tiernogalvan.eternaquest.model.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ZonaRepository extends JpaRepository<Zona, Long> {
    List<Zona> findByNivelRequeridoLessThanEqualOrderByNivelRequerido(int nivel);
}
