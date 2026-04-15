package ies.tiernogalvan.eternaquest.repository;

import ies.tiernogalvan.eternaquest.model.entity.Enemigo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface EnemigoRepository extends JpaRepository<Enemigo, Long> {
    List<Enemigo> findByZonaId(Long zonaId);

    @Query(value = "SELECT * FROM enemigos WHERE zona_id = :zonaId ORDER BY RANDOM() LIMIT 1",
           nativeQuery = true)
    Optional<Enemigo> findRandomByZonaId(Long zonaId);
}
