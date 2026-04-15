package ies.tiernogalvan.eternaquest.service.impl;

import ies.tiernogalvan.eternaquest.config.GameConfig;
import ies.tiernogalvan.eternaquest.model.dto.websocket.InvasionEventMessage;
import ies.tiernogalvan.eternaquest.model.entity.*;
import ies.tiernogalvan.eternaquest.model.enums.*;
import ies.tiernogalvan.eternaquest.repository.*;
import ies.tiernogalvan.eternaquest.service.interfaces.ICombateService;
import ies.tiernogalvan.eternaquest.service.interfaces.IPersonajeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service @RequiredArgsConstructor
public class InvasionServiceImpl {

    private final GameConfig gameConfig;
    private final IPersonajeService personajeService;
    private final ICombateService combateService;
    private final PersonajeRepository personajeRepository;
    private final EstadisticasJugadorRepository statsRepository;
    private final CombateRepository combateRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, SesionInvasion> sesiones = new ConcurrentHashMap<>();

    public record SesionInvasion(
            String sessionId,
            Personaje host,
            Personaje invasorOriginal,
            Personaje invasorEscalado,
            int vidaHost,
            int vidaInvasor,
            boolean turnoHost
    ) {}


    @Transactional
    public boolean intentarInvasion(String hostEmail, Long zonaId) {
        Personaje host = personajeService.getPersonajeEntity(hostEmail);

        List<Personaje> candidatos = personajeRepository
                .findByZonaActualIdAndIdNot(zonaId, host.getId());

        if (candidatos.isEmpty()) return false;

        if (Math.random() > gameConfig.getInvasionProbability()) return false;

        Personaje invasorOriginal = candidatos.get(new Random().nextInt(candidatos.size()));
        Personaje invasorEscalado = combateService.escalarANivel(invasorOriginal, host.getNivel());

        String sessionId = UUID.randomUUID().toString();
        SesionInvasion sesion = new SesionInvasion(
                sessionId, host, invasorOriginal, invasorEscalado,
                host.getVidaActual(), invasorEscalado.getVidaActual(), true
        );
        sesiones.put(sessionId, sesion);

        InvasionEventMessage msgHost = InvasionEventMessage.builder()
                .tipo(InvasionEventMessage.Tipo.INVASION_INICIADA)
                .sessionId(sessionId)
                .invasorNombre(invasorOriginal.getNombre())
                .hostNombre(host.getNombre())
                .invasorNivel(invasorEscalado.getNivel())
                .hostNivel(host.getNivel())
                .vidaHost(host.getVidaActual())
                .vidaInvasor(invasorEscalado.getVidaActual())
                .logMensaje("¡" + invasorOriginal.getNombre() + " ha invadido tu mundo!")
                .build();
        messagingTemplate.convertAndSendToUser(hostEmail, "/queue/invasion", msgHost);

        String invasorEmail = invasorOriginal.getUsuario().getEmail();
        InvasionEventMessage msgInvasor = InvasionEventMessage.builder()
                .tipo(InvasionEventMessage.Tipo.INVASION_INICIADA)
                .sessionId(sessionId)
                .invasorNombre(invasorOriginal.getNombre())
                .hostNombre(host.getNombre())
                .invasorNivel(invasorEscalado.getNivel())
                .hostNivel(host.getNivel())
                .vidaHost(host.getVidaActual())
                .vidaInvasor(invasorEscalado.getVidaActual())
                .logMensaje("¡Estás invadiendo el mundo de " + host.getNombre() + "!")
                .build();
        messagingTemplate.convertAndSendToUser(invasorEmail, "/queue/invasion", msgInvasor);

        log.info("Invasión iniciada: session={}, host={}, invasor={}", sessionId, hostEmail, invasorEmail);
        return true;
    }


    @Transactional
    public void procesarAccion(String sessionId, String emailJugador, String accion) {
        SesionInvasion sesion = sesiones.get(sessionId);
        if (sesion == null) return;

        boolean esHost = sesion.host().getUsuario().getEmail().equals(emailJugador);
        boolean esTurnoValido = (esHost && sesion.turnoHost()) || (!esHost && !sesion.turnoHost());
        if (!esTurnoValido) return;

        String hostEmail    = sesion.host().getUsuario().getEmail();
        String invasorEmail = sesion.invasorOriginal().getUsuario().getEmail();

        int vidaHost    = sesion.vidaHost();
        int vidaInvasor = sesion.vidaInvasor();
        String logMsg;

        if (esHost) {
            int danio = combateService.calcularDanio(sesion.host().getAtaque(), sesion.invasorEscalado().getDefensa());
            vidaInvasor = Math.max(0, vidaInvasor - danio);
            logMsg = sesion.host().getNombre() + " ataca por " + danio + " de daño. ";
        } else {
            int danio = combateService.calcularDanio(sesion.invasorEscalado().getAtaque(), sesion.host().getDefensa());
            vidaHost = Math.max(0, vidaHost - danio);
            logMsg = sesion.invasorOriginal().getNombre() + " ataca por " + danio + " de daño. ";
        }

        if (vidaHost <= 0 || vidaInvasor <= 0) {
            boolean hostGana = vidaInvasor <= 0;
            finalizarInvasion(sessionId, sesion, hostGana, logMsg);
            return;
        }

        SesionInvasion actualizada = new SesionInvasion(
                sessionId, sesion.host(), sesion.invasorOriginal(),
                sesion.invasorEscalado(), vidaHost, vidaInvasor, !sesion.turnoHost()
        );
        sesiones.put(sessionId, actualizada);

        String turno = !sesion.turnoHost() ? "TURNO_INVASOR" : "TURNO_JUGADOR";
        InvasionEventMessage estado = InvasionEventMessage.builder()
                .tipo(InvasionEventMessage.Tipo.valueOf(turno))
                .sessionId(sessionId)
                .vidaHost(vidaHost)
                .vidaInvasor(vidaInvasor)
                .logMensaje(logMsg)
                .build();
        messagingTemplate.convertAndSendToUser(hostEmail,    "/queue/invasion", estado);
        messagingTemplate.convertAndSendToUser(invasorEmail, "/queue/invasion", estado);
    }

    private void finalizarInvasion(String sessionId, SesionInvasion sesion, boolean hostGana, String logBase) {
        sesiones.remove(sessionId);
        String hostEmail    = sesion.host().getUsuario().getEmail();
        String invasorEmail = sesion.invasorOriginal().getUsuario().getEmail();

        EstadisticasJugador statsHost = statsRepository
                .findByPersonajeId(sesion.host().getId()).orElseThrow();
        EstadisticasJugador statsInvasor = statsRepository
                .findByPersonajeId(sesion.invasorOriginal().getId()).orElseThrow();

        if (hostGana) {
            statsHost.registrarVictoria(true);
            statsInvasor.registrarDerrota(true);
        } else {
            statsHost.registrarDerrota(true);
            statsInvasor.registrarVictoria(true);
        }
        statsRepository.save(statsHost);
        statsRepository.save(statsInvasor);

        combateRepository.save(Combate.builder()
                .personaje(sesion.host())
                .personaje2(sesion.invasorOriginal())
                .tipo(TipoCombate.PVP_INVASION)
                .resultado(hostGana ? ResultadoCombate.VICTORIA : ResultadoCombate.DERROTA)
                .build());

        String resultado = hostGana ? "HOST_GANA" : "INVASOR_GANA";
        String logFinal = logBase + (hostGana
                ? sesion.host().getNombre() + " ha repelido la invasión."
                : sesion.invasorOriginal().getNombre() + " ha completado la invasión con éxito.");

        InvasionEventMessage fin = InvasionEventMessage.builder()
                .tipo(InvasionEventMessage.Tipo.FIN_COMBATE)
                .sessionId(sessionId)
                .resultado(resultado)
                .logMensaje(logFinal)
                .vidaHost(hostGana ? sesion.vidaHost() : 0)
                .vidaInvasor(hostGana ? 0 : sesion.vidaInvasor())
                .build();

        messagingTemplate.convertAndSendToUser(hostEmail,    "/queue/invasion", fin);
        messagingTemplate.convertAndSendToUser(invasorEmail, "/queue/invasion", fin);
    }

    public Optional<SesionInvasion> getSesion(String sessionId) {
        return Optional.ofNullable(sesiones.get(sessionId));
    }
}
