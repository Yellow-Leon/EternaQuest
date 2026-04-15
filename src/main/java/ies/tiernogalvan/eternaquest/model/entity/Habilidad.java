package ies.tiernogalvan.eternaquest.model.entity;

import ies.tiernogalvan.eternaquest.model.enums.ClasePersonaje;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "habilidades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Habilidad {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClasePersonaje clase;

    @Column(nullable = false) private String nombre;
    private String efecto;
    @Column(nullable = false) private int costeMana;
    @Column(nullable = false) private int nivelRequerido;
    @Column(nullable = false) private int multiplicadorDanio;
}
