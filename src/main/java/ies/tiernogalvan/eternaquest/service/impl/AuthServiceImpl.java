package ies.tiernogalvan.eternaquest.service.impl;

import ies.tiernogalvan.eternaquest.model.dto.request.*;
import ies.tiernogalvan.eternaquest.model.dto.response.AuthResponse;
import ies.tiernogalvan.eternaquest.model.entity.Usuario;
import ies.tiernogalvan.eternaquest.model.enums.RolUsuario;
import ies.tiernogalvan.eternaquest.repository.UsuarioRepository;
import ies.tiernogalvan.eternaquest.security.JwtTokenProvider;
import ies.tiernogalvan.eternaquest.service.interfaces.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(RolUsuario.JUGADOR)
                .build();
        usuarioRepository.save(usuario);
        String token = jwtTokenProvider.generarToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getEmail(), usuario.getRol().name(), false);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        String token = jwtTokenProvider.generarToken(email);
        boolean tienePersonaje = usuario.getPersonaje() != null;
        return new AuthResponse(token, email, usuario.getRol().name(), tienePersonaje);
    }

    @Override
    public void solicitarRecuperacionPassword(String email) {
        // TODO: implementar envío de email con token de recuperación
    }
}
