package ies.tiernogalvan.eternaquest.service.impl;

import ies.tiernogalvan.eternaquest.model.dto.request.AccionCombateRequest;
import ies.tiernogalvan.eternaquest.model.dto.response.CombateTurnoResponse;
import ies.tiernogalvan.eternaquest.model.entity.*;
import ies.tiernogalvan.eternaquest.model.enums.*;
import ies.tiernogalvan.eternaquest.repository.*;
import ies.tiernogalvan.eternaquest.service.interfaces.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service @RequiredArgsConstructor
public class CombateServiceImpl implements ICombateService {

    private final IPersonajeService personajeService;
    private final EnemigoRepository enemigoRepository;
    private final CombateRepository combateRepository;
    private final EstadisticasJugadorRepository statsRepository;
    private final HabilidadRepository habilidadRepository;
    private final InventarioItemRepository inventarioRepository;
    private final PersonajeRepository personajeRepository;

    private final Map<String, SesionCombatePve> sesionesActivas = new ConcurrentHashMap<>();

    private record SesionCombatePve(Personaje personaje, Enemigo enemigo, int vidaEnemigo) {}

    @Override
    @Transactional
    public CombateTurnoResponse iniciarCombate(String email, Long zonaId) {
        Personaje personaje = personajeService.getPersonajeEntity(email);
        Enemigo enemigo = enemigoRepository.findRandomByZonaId(zonaId)
                .orElseThrow(() -> new IllegalStateException("No hay enemigos en esta zona"));

        sesionesActivas.put(email, new SesionCombatePve(personaje, enemigo, enemigo.getVida()));

        return CombateTurnoResponse.builder()
                .fase("JUGADOR_TURNO")
                .vidaJugador(personaje.getVidaActual())
                .vidaEnemigo(enemigo.getVida())
                .manaJugador(personaje.getMana())
                .logMensaje("Un " + enemigo.getNombre() + " aparece. ¡Es tu turno!")
                .combateTerminado(false)
                .build();
    }

    @Override
    @Transactional
    public CombateTurnoResponse ejecutarAccion(String email, AccionCombateRequest request) {
        SesionCombatePve sesion = sesionesActivas.get(email);
        if (sesion == null) throw new IllegalStateException("No hay combate activo");

        Personaje personaje = sesion.personaje();
        Enemigo enemigo = sesion.enemigo();
        int vidaEnemigo = sesion.vidaEnemigo();
        StringBuilder log = new StringBuilder();

        switch (request.getAccion()) {
            case ATACAR -> {
                int danio = calcularDanio(personaje.getAtaque(), enemigo.getDefensa());
                vidaEnemigo -= danio;
                log.append("Atacas al ").append(enemigo.getNombre()).append(" por ").append(danio).append(" de daño. ");
            }
            case HABILIDAD -> {
                if (request.getHabilidadId() == null) throw new IllegalArgumentException("Falta habilidadId");
                Habilidad hab = habilidadRepository.findById(request.getHabilidadId())
                        .orElseThrow(() -> new IllegalArgumentException("Habilidad no encontrada"));
                if (personaje.getMana() < hab.getCosteMana()) {
                    return CombateTurnoResponse.builder().fase("JUGADOR_TURNO")
                            .vidaJugador(personaje.getVidaActual()).vidaEnemigo(vidaEnemigo)
                            .manaJugador(personaje.getMana())
                            .logMensaje("¡Mana insuficiente para " + hab.getNombre() + "!")
                            .combateTerminado(false).build();
                }
                personaje.setMana(personaje.getMana() - hab.getCosteMana());
                int danio = calcularDanio(personaje.getAtaque() * hab.getMultiplicadorDanio(), enemigo.getDefensa());
                vidaEnemigo -= danio;
                log.append("Usas ").append(hab.getNombre()).append(" por ").append(danio).append(" de daño. ");
            }
            case OBJETO -> {
                if (request.getObjetoId() == null) throw new IllegalArgumentException("Falta objetoId");
                InventarioItem item = inventarioRepository
                        .findByPersonajeIdAndObjetoId(personaje.getId(), request.getObjetoId())
                        .orElseThrow(() -> new IllegalArgumentException("Objeto no en inventario"));
                if (item.getObjeto().getTipo() != TipoObjeto.CONSUMIBLE)
                    throw new IllegalArgumentException("El objeto no es consumible");
                personaje.setVidaActual(Math.min(personaje.getVidaMax(),
                        personaje.getVidaActual() + item.getObjeto().getValorBonus()));
                item.setCantidad(item.getCantidad() - 1);
                if (item.getCantidad() == 0) inventarioRepository.delete(item);
                else inventarioRepository.save(item);
                log.append("Usas ").append(item.getObjeto().getNombre()).append(". Recuperas ").append(item.getObjeto().getValorBonus()).append(" HP. ");
            }
            case HUIR -> {
                boolean huida = Math.random() < (personaje.getVelocidad() / (double)(personaje.getVelocidad() + enemigo.getVelocidad()));
                sesionesActivas.remove(email);
                String resultado = huida ? "¡Huiste exitosamente!" : "¡No pudiste escapar!";
                if (huida) {
                    return CombateTurnoResponse.builder().fase("HUIDA").vidaJugador(personaje.getVidaActual())
                            .vidaEnemigo(vidaEnemigo).manaJugador(personaje.getMana())
                            .logMensaje(resultado).combateTerminado(true).build();
                }
                log.append(resultado).append(" ");
            }
        }

        if (vidaEnemigo <= 0) {
            return procesarVictoria(email, personaje, enemigo, log.toString());
        }

        int danioEnemigo = calcularDanio(enemigo.getAtaque(), personaje.getDefensa());
        personaje.setVidaActual(personaje.getVidaActual() - danioEnemigo);
        log.append(enemigo.getNombre()).append(" te ataca por ").append(danioEnemigo).append(" de daño.");
        personajeRepository.save(personaje);

        sesionesActivas.put(email, new SesionCombatePve(personaje, enemigo, vidaEnemigo));

        if (personaje.getVidaActual() <= 0) {
            return procesarDerrota(email, personaje, enemigo, log.toString());
        }

        return CombateTurnoResponse.builder()
                .fase("JUGADOR_TURNO")
                .vidaJugador(personaje.getVidaActual())
                .vidaEnemigo(vidaEnemigo)
                .manaJugador(personaje.getMana())
                .logMensaje(log.toString())
                .combateTerminado(false)
                .build();
    }

    private CombateTurnoResponse procesarVictoria(String email, Personaje personaje, Enemigo enemigo, String logPrevio) {
        sesionesActivas.remove(email);
        personaje.setExperiencia(personaje.getExperiencia() + enemigo.getExpRecompensa());
        personaje.setOro(personaje.getOro() + enemigo.getOroRecompensa());
        personajeRepository.save(personaje);

        EstadisticasJugador stats = statsRepository.findByPersonajeId(personaje.getId()).orElseThrow();
        stats.registrarVictoria(false);
        statsRepository.save(stats);

        combateRepository.save(Combate.builder()
                .personaje(personaje).enemigo(enemigo)
                .tipo(TipoCombate.PVE).resultado(ResultadoCombate.VICTORIA).build());

        boolean subioNivel = personaje.puedeSubirNivel();

        return CombateTurnoResponse.builder()
                .fase("VICTORIA")
                .vidaJugador(personaje.getVidaActual())
                .vidaEnemigo(0)
                .manaJugador(personaje.getMana())
                .logMensaje(logPrevio + " ¡Victoria! +" + enemigo.getExpRecompensa() + " EXP, +" + enemigo.getOroRecompensa() + " oro."
                        + (subioNivel ? " ¡SUBISTE DE NIVEL!" : ""))
                .expGanada(enemigo.getExpRecompensa())
                .oroGanado(enemigo.getOroRecompensa())
                .combateTerminado(true)
                .build();
    }

    private CombateTurnoResponse procesarDerrota(String email, Personaje personaje, Enemigo enemigo, String logPrevio) {
        sesionesActivas.remove(email);
        personaje.setVidaActual(1);
        personajeRepository.save(personaje);

        EstadisticasJugador stats = statsRepository.findByPersonajeId(personaje.getId()).orElseThrow();
        stats.registrarDerrota(false);
        statsRepository.save(stats);

        combateRepository.save(Combate.builder()
                .personaje(personaje).enemigo(enemigo)
                .tipo(TipoCombate.PVE).resultado(ResultadoCombate.DERROTA).build());

        return CombateTurnoResponse.builder()
                .fase("DERROTA")
                .vidaJugador(1)
                .vidaEnemigo(enemigo.getVida())
                .manaJugador(personaje.getMana())
                .logMensaje(logPrevio + " Has sido derrotado. Regresas al mapa con 1 HP.")
                .combateTerminado(true)
                .build();
    }

    @Override
    public int calcularDanio(int ataque, int defensa) {
        return Math.max(1, ataque - defensa + new Random().nextInt(5) - 2);
    }

    @Override
    public Personaje escalarANivel(Personaje invasor, int nivelObjetivo) {
        int ratio = nivelObjetivo - invasor.getNivel();
        if (ratio <= 0) return invasor;
        Personaje escalado = new Personaje();
        escalado.setId(invasor.getId());
        escalado.setNombre(invasor.getNombre());
        escalado.setClase(invasor.getClase());
        escalado.setNivel(nivelObjetivo);
        escalado.setAtaque(invasor.getAtaque() + ratio * 2);
        escalado.setDefensa(invasor.getDefensa() + ratio);
        escalado.setVelocidad(invasor.getVelocidad() + ratio);
        escalado.setMagia(invasor.getMagia() + ratio);
        int vidaMax = invasor.getVidaMax() + ratio * 10;
        escalado.setVidaMax(vidaMax);
        escalado.setVidaActual(vidaMax);
        escalado.setManaMax(invasor.getManaMax() + ratio * 5);
        escalado.setMana(escalado.getManaMax());
        return escalado;
    }
}
