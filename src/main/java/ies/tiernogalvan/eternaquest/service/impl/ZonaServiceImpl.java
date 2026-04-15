package ies.tiernogalvan.eternaquest.service.impl;

import ies.tiernogalvan.eternaquest.model.dto.response.ZonaResponse;
import ies.tiernogalvan.eternaquest.model.entity.Personaje;
import ies.tiernogalvan.eternaquest.model.entity.Zona;
import ies.tiernogalvan.eternaquest.repository.PersonajeRepository;
import ies.tiernogalvan.eternaquest.repository.ZonaRepository;
import ies.tiernogalvan.eternaquest.service.interfaces.IPersonajeService;
import ies.tiernogalvan.eternaquest.service.interfaces.IZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class ZonaServiceImpl implements IZonaService {

    private final ZonaRepository zonaRepository;
    private final PersonajeRepository personajeRepository;
    private final IPersonajeService personajeService;

    @Override
    public List<ZonaResponse> listarZonas(String email) {
        Personaje personaje = personajeService.getPersonajeEntity(email);
        return zonaRepository.findAll().stream()
                .map(z -> ZonaResponse.from(z, personaje.getNivel()))
                .toList();
    }

    @Override
    @Transactional
    public ZonaResponse entrarZona(String email, Long zonaId) {
        Personaje personaje = personajeService.getPersonajeEntity(email);
        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada"));

        if (personaje.getNivel() < zona.getNivelRequerido()) {
            throw new IllegalStateException("Nivel insuficiente para entrar en esta zona");
        }
        personaje.setZonaActual(zona);
        personajeRepository.save(personaje);
        return ZonaResponse.from(zona, personaje.getNivel());
    }

    @Override
    @Transactional
    public void salirZona(String email) {
        Personaje personaje = personajeService.getPersonajeEntity(email);
        personaje.setZonaActual(null);
        personajeRepository.save(personaje);
    }
}
