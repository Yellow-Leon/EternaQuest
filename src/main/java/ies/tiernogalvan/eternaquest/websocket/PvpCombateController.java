package ies.tiernogalvan.eternaquest.websocket;

import ies.tiernogalvan.eternaquest.model.dto.websocket.PvpAccionMessage;
import ies.tiernogalvan.eternaquest.service.impl.InvasionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller @RequiredArgsConstructor
public class PvpCombateController {

    private final InvasionServiceImpl invasionService;


    @MessageMapping("/invasion/accion")
    public void procesarAccion(PvpAccionMessage mensaje, Principal principal) {
        if (mensaje.getSessionId() == null || principal == null) return;
        invasionService.procesarAccion(
                mensaje.getSessionId(),
                principal.getName(),
                mensaje.getAccion() != null ? mensaje.getAccion().name() : "ATACAR"
        );
    }
}
