package ies.tiernogalvan.eternaquest.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "enemigos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Enemigo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Column(nullable = false) private String nombre;
    @Column(nullable = false) private int vida;
    @Column(nullable = false) private int ataque;
    @Column(nullable = false) private int defensa;
    @Column(nullable = false) private int velocidad;
    @Column(nullable = false) private int expRecompensa;
    @Column(nullable = false) private int oroRecompensa;
}
