package ies.tiernogalvan.eternaquest.controller;

import ies.tiernogalvan.eternaquest.model.dto.response.CombateTurnoResponse;
import ies.tiernogalvan.eternaquest.model.dto.response.ZonaResponse;
import ies.tiernogalvan.eternaquest.service.impl.InvasionServiceImpl;
import ies.tiernogalvan.eternaquest.service.interfaces.ICombateService;
import ies.tiernogalvan.eternaquest.service.interfaces.IZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
public class ZonaController {

    private final IZonaService zonaService;
    private final ICombateService combateService;
    private final InvasionServiceImpl invasionService;

    @GetMapping
    public ResponseEntity<List<ZonaResponse>> listar(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(zonaService.listarZonas(user.getUsername()));
    }

    @PostMapping("/{id}/entrar")
    public ResponseEntity<Map<String, Object>> entrar(@AuthenticationPrincipal UserDetails user,
                                                       @PathVariable Long id) {
        ZonaResponse zona = zonaService.entrarZona(user.getUsername(), id);
        boolean invadido  = invasionService.intentarInvasion(user.getUsername(), id);
        return ResponseEntity.ok(Map.of(
                "zona", zona,
                "invasion", invadido
        ));
    }

    @PostMapping("/salir")
    public ResponseEntity<Void> salir(@AuthenticationPrincipal UserDetails user) {
        zonaService.salirZona(user.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/combate/iniciar")
    public ResponseEntity<CombateTurnoResponse> iniciarCombate(
            @AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        return ResponseEntity.ok(combateService.iniciarCombate(user.getUsername(), id));
    }
}
