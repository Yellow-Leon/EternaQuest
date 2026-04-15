package ies.tiernogalvan.eternaquest.model.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InvasionEventMessage {
    public enum Tipo { INVASION_INICIADA, TURNO_JUGADOR, TURNO_INVASOR, FIN_COMBATE }

    private Tipo tipo;
    private String invasorNombre;
    private String hostNombre;
    private int invasorNivel;
    private int hostNivel;
    private int vidaInvasor;
    private int vidaHost;
    private String logMensaje;
    private String resultado;
    private String sessionId;
}
