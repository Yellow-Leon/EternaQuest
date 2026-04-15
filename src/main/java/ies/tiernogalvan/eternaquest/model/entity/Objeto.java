package ies.tiernogalvan.eternaquest.model.entity;

import ies.tiernogalvan.eternaquest.model.enums.TipoObjeto;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "objetos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Objeto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoObjeto tipo;

    private String efecto;
    @Column(nullable = false) private int precio;
    private String bonusStat;
    @Column(nullable = false) private int valorBonus;
}
