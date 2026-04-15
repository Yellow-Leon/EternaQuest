package ies.tiernogalvan.eternaquest.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "inventario",
    uniqueConstraints = @UniqueConstraint(columnNames = {"personaje_id","objeto_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventarioItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personaje_id", nullable = false)
    private Personaje personaje;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "objeto_id", nullable = false)
    private Objeto objeto;

    @Column(nullable = false) private int cantidad = 1;
    @Column(nullable = false) private boolean equipado = false;
}
