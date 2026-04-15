package ies.tiernogalvan.eternaquest.service.interfaces;

import ies.tiernogalvan.eternaquest.model.dto.request.LoginRequest;
import ies.tiernogalvan.eternaquest.model.dto.request.RegisterRequest;
import ies.tiernogalvan.eternaquest.model.dto.response.AuthResponse;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void solicitarRecuperacionPassword(String email);
}
