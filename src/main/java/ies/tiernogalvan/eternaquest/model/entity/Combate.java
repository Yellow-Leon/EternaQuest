package ies.tiernogalvan.eternaquest.model.entity;

import ies.tiernogalvan.eternaquest.model.enums.ResultadoCombate;
import ies.tiernogalvan.eternaquest.model.enums.TipoCombate;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "combates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Combate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personaje_id", nullable = false)
    private Personaje personaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enemigo_id")
    private Enemigo enemigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personaje2_id")
    private Personaje personaje2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCombate tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoCombate resultado;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
