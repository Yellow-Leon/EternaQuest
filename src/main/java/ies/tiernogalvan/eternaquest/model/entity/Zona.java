package ies.tiernogalvan.eternaquest.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "zonas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Zona {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String nombre;
    @Column(nullable = false) private int nivelRequerido;
    private String descripcion;

    @OneToMany(mappedBy = "zona", fetch = FetchType.LAZY)
    private List<Enemigo> enemigos;
}
