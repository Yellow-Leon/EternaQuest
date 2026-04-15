package ies.tiernogalvan.eternaquest.controller;

import ies.tiernogalvan.eternaquest.model.dto.request.LoginRequest;
import ies.tiernogalvan.eternaquest.model.dto.request.RegisterRequest;
import ies.tiernogalvan.eternaquest.model.dto.response.AuthResponse;
import ies.tiernogalvan.eternaquest.service.interfaces.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<Void> recuperarPassword(@RequestParam String email) {
        authService.solicitarRecuperacionPassword(email);
        return ResponseEntity.ok().build();
    }
}
