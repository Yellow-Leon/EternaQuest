package ies.tiernogalvan.eternaquest.controller;

import ies.tiernogalvan.eternaquest.model.entity.EstadisticasJugador;
import ies.tiernogalvan.eternaquest.service.interfaces.IRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final IRankingService rankingService;

    @GetMapping
    public ResponseEntity<List<EstadisticasJugador>> getRanking(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {
        return ResponseEntity.ok(rankingService.getRanking(pagina, tamanio));
    }

    @GetMapping("/mis-estadisticas")
    public ResponseEntity<EstadisticasJugador> getMisEstadisticas(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(rankingService.getMisEstadisticas(user.getUsername()));
    }
}
