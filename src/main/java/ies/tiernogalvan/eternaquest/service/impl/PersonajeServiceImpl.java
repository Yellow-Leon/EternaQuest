package ies.tiernogalvan.eternaquest.service.impl;

import ies.tiernogalvan.eternaquest.config.GameConfig;
import ies.tiernogalvan.eternaquest.model.dto.request.CrearPersonajeRequest;
import ies.tiernogalvan.eternaquest.model.dto.response.PersonajeResponse;
import ies.tiernogalvan.eternaquest.model.entity.*;
import ies.tiernogalvan.eternaquest.repository.*;
import ies.tiernogalvan.eternaquest.service.interfaces.IPersonajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class PersonajeServiceImpl implements IPersonajeService {

    private final UsuarioRepository usuarioRepository;
    private final PersonajeRepository personajeRepository;
    private final EstadisticasJugadorRepository statsRepository;
    private final GameConfig gameConfig;

    @Override
    @Transactional
    public PersonajeResponse crearPersonaje(String email, CrearPersonajeRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        if (usuario.getPersonaje() != null) {
            throw new IllegalStateException("El usuario ya tiene un personaje");
        }
        GameConfig.StatsBase stats = gameConfig.getStatsBase(request.getClase());
        Personaje personaje = Personaje.builder()
                .usuario(usuario)
                .nombre(request.getNombre())
                .clase(request.getClase())
                .vidaActual(stats.vida()).vidaMax(stats.vida())
                .ataque(stats.ataque()).defensa(stats.defensa())
                .velocidad(stats.velocidad()).magia(stats.magia())
                .mana(stats.mana()).manaMax(stats.mana())
                .build();
        personajeRepository.save(personaje);

        EstadisticasJugador estadisticas = EstadisticasJugador.builder()
                .personaje(personaje).build();
        statsRepository.save(estadisticas);
        return PersonajeResponse.from(personaje);
    }

    @Override
    public PersonajeResponse getPersonaje(String email) {
        return PersonajeResponse.from(getPersonajeEntity(email));
    }

    @Override
    public Personaje getPersonajeEntity(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        return personajeRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene personaje"));
    }

    @Override
    @Transactional
    public PersonajeResponse subirNivel(Personaje personaje) {
        while (personaje.puedeSubirNivel()) {
            personaje.setExperiencia(personaje.getExperiencia() - personaje.getExpParaSiguienteNivel());
            personaje.setNivel(personaje.getNivel() + 1);
            personaje.setVidaMax(personaje.getVidaMax() + 10);
            personaje.setAtaque(personaje.getAtaque() + 2);
            personaje.setDefensa(personaje.getDefensa() + 1);
            personaje.setManaMax(personaje.getManaMax() + 5);
        }
        personaje.setVidaActual(personaje.getVidaMax());
        personaje.setMana(personaje.getManaMax());
        personajeRepository.save(personaje);
        return PersonajeResponse.from(personaje);
    }

    @Override
    @Transactional
    public PersonajeResponse distribuirPuntos(String email, int vida, int ataque, int defensa, int velocidad, int magia) {
        Personaje p = getPersonajeEntity(email);
        p.setVidaMax(p.getVidaMax() + vida);
        p.setAtaque(p.getAtaque() + ataque);
        p.setDefensa(p.getDefensa() + defensa);
        p.setVelocidad(p.getVelocidad() + velocidad);
        p.setMagia(p.getMagia() + magia);
        personajeRepository.save(p);
        return PersonajeResponse.from(p);
    }
}
