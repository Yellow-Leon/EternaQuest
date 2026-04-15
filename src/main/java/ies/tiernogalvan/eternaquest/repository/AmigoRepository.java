package ies.tiernogalvan.eternaquest.repository;

import ies.tiernogalvan.eternaquest.model.entity.Amigo;
import ies.tiernogalvan.eternaquest.model.enums.EstadoAmistad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AmigoRepository extends JpaRepository<Amigo, Long> {

    @Query("SELECT a FROM Amigo a WHERE (a.solicitante.id = :userId OR a.receptor.id = :userId) AND a.estado = :estado")
    List<Amigo> findRelacionesByUsuarioIdAndEstado(Long userId, EstadoAmistad estado);
}
