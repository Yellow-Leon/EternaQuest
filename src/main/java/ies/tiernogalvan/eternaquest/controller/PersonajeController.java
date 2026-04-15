package ies.tiernogalvan.eternaquest.controller;

import ies.tiernogalvan.eternaquest.model.dto.request.CrearPersonajeRequest;
import ies.tiernogalvan.eternaquest.model.dto.response.PersonajeResponse;
import ies.tiernogalvan.eternaquest.model.entity.Habilidad;
import ies.tiernogalvan.eternaquest.model.entity.Personaje;
import ies.tiernogalvan.eternaquest.repository.HabilidadRepository;
import ies.tiernogalvan.eternaquest.service.interfaces.IPersonajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/personaje")
@RequiredArgsConstructor
public class PersonajeController {

    private final IPersonajeService personajeService;
    private final HabilidadRepository habilidadRepository;

    @PostMapping
    public ResponseEntity<PersonajeResponse> crear(@AuthenticationPrincipal UserDetails user,
                                                    @Valid @RequestBody CrearPersonajeRequest request) {
        return ResponseEntity.ok(personajeService.crearPersonaje(user.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<PersonajeResponse> getMiPersonaje(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(personajeService.getPersonaje(user.getUsername()));
    }

    @PostMapping("/subir-nivel")
    public ResponseEntity<PersonajeResponse> subirNivel(@AuthenticationPrincipal UserDetails user) {
        Personaje p = personajeService.getPersonajeEntity(user.getUsername());
        return ResponseEntity.ok(personajeService.subirNivel(p));
    }

    @PostMapping("/distribuir-puntos")
    public ResponseEntity<PersonajeResponse> distribuirPuntos(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody Map<String, Integer> puntos) {
        return ResponseEntity.ok(personajeService.distribuirPuntos(
                user.getUsername(),
                puntos.getOrDefault("vida", 0),
                puntos.getOrDefault("ataque", 0),
                puntos.getOrDefault("defensa", 0),
                puntos.getOrDefault("velocidad", 0),
                puntos.getOrDefault("magia", 0)
        ));
    }

    @GetMapping("/habilidades")
    public ResponseEntity<List<Habilidad>> getHabilidades(@AuthenticationPrincipal UserDetails user) {
        Personaje p = personajeService.getPersonajeEntity(user.getUsername());
        return ResponseEntity.ok(habilidadRepository
                .findByClaseAndNivelRequeridoLessThanEqual(p.getClase(), p.getNivel()));
    }
}
