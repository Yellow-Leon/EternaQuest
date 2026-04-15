package ies.tiernogalvan.eternaquest.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CombateTurnoResponse {
    private String fase;
    private int vidaJugador;
    private int vidaEnemigo;
    private int manaJugador;
    private String logMensaje;
    private int expGanada;
    private int oroGanado;
    private boolean combateTerminado;
}
