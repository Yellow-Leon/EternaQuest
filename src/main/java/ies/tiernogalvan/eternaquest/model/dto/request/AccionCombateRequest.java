package ies.tiernogalvan.eternaquest.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccionCombateRequest {
    public enum TipoAccion { ATACAR, HABILIDAD, OBJETO, HUIR }
    @NotNull private TipoAccion accion;
    private Long habilidadId;
    private Long objetoId;
}
