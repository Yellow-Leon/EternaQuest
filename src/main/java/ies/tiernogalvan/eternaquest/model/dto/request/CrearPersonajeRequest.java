package ies.tiernogalvan.eternaquest.model.dto.request;

import ies.tiernogalvan.eternaquest.model.enums.ClasePersonaje;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearPersonajeRequest {
    @NotBlank @Size(min = 3, max = 20) private String nombre;
    @NotNull private ClasePersonaje clase;
}
