package ies.tiernogalvan.eternaquest.controller;

import ies.tiernogalvan.eternaquest.model.dto.request.AccionCombateRequest;
import ies.tiernogalvan.eternaquest.model.dto.response.CombateTurnoResponse;
import ies.tiernogalvan.eternaquest.service.interfaces.ICombateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/combate")
@RequiredArgsConstructor
public class CombateController {

    private final ICombateService combateService;

    @PostMapping("/accion")
    public ResponseEntity<CombateTurnoResponse> ejecutarAccion(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody AccionCombateRequest request) {
        return ResponseEntity.ok(combateService.ejecutarAccion(user.getUsername(), request));
    }
}
