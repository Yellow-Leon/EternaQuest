package ies.tiernogalvan.eternaquest.model.dto.websocket;

import lombok.Data;

@Data
public class PvpAccionMessage {
    public enum TipoAccion { ATACAR, HABILIDAD, OBJETO }
    private TipoAccion accion;
    private Long habilidadId;
    private Long objetoId;
    private String sessionId;
}
