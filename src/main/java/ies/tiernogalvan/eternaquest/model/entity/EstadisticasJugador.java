package ies.tiernogalvan.eternaquest.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "estadisticas_jugador")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstadisticasJugador {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personaje_id", unique = true, nullable = false)
    private Personaje personaje;

    @Column(nullable = false) private int victoriasPve = 0;
    @Column(nullable = false) private int derrotasPve = 0;
    @Column(nullable = false) private int victoriasPvp = 0;
    @Column(nullable = false) private int derrotasPvp = 0;
    @Column(nullable = false) private int rachaActual = 0;
    @Column(nullable = false) private int rachaMaxima = 0;
    @Column(nullable = false) private int puntosRanking = 0;

    public void registrarVictoria(boolean esPvp) {
        if (esPvp) { victoriasPvp++; puntosRanking += 15; }
        else        { victoriasPve++;  puntosRanking += 5; }
        rachaActual++;
        if (rachaActual > rachaMaxima) rachaMaxima = rachaActual;
    }

    public void registrarDerrota(boolean esPvp) {
        if (esPvp) { derrotasPvp++; puntosRanking = Math.max(0, puntosRanking - 10); }
        else        { derrotasPve++; puntosRanking = Math.max(0, puntosRanking - 3); }
        rachaActual = 0;
    }
}
