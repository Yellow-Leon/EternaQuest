package ies.tiernogalvan.eternaquest.model.entity;

import ies.tiernogalvan.eternaquest.model.enums.EstadoAmistad;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "amigos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Amigo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptor_id", nullable = false)
    private Usuario receptor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAmistad estado = EstadoAmistad.PENDIENTE;

    private LocalDateTime fechaSolicitud = LocalDateTime.now();
    private LocalDateTime fechaRespuesta;
}
