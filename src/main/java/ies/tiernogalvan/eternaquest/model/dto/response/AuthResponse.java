package ies.tiernogalvan.eternaquest.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String rol;
    private boolean tienePersonaje;
}
