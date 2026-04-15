package ies.tiernogalvan.eternaquest.model.entity;

import ies.tiernogalvan.eternaquest.model.enums.ClasePersonaje;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "personajes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Personaje {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClasePersonaje clase;

    @Column(nullable = false) private int nivel = 1;
    @Column(nullable = false) private int experiencia = 0;
    @Column(nullable = false) private int oro = 50;
    @Column(nullable = false) private int vidaActual;
    @Column(nullable = false) private int vidaMax;
    @Column(nullable = false) private int ataque;
    @Column(nullable = false) private int defensa;
    @Column(nullable = false) private int velocidad;
    @Column(nullable = false) private int magia;
    @Column(nullable = false) private int mana;
    @Column(nullable = false) private int manaMax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_actual_id")
    private Zona zonaActual;

    @OneToOne(mappedBy = "personaje", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EstadisticasJugador estadisticas;

    public int getExpParaSiguienteNivel() {
        return nivel * 100;
    }

    public boolean puedeSubirNivel() {
        return experiencia >= getExpParaSiguienteNivel();
    }
}
