package ies.tiernogalvan.eternaquest.service.impl;

import ies.tiernogalvan.eternaquest.model.entity.EstadisticasJugador;
import ies.tiernogalvan.eternaquest.model.entity.Personaje;
import ies.tiernogalvan.eternaquest.repository.EstadisticasJugadorRepository;
import ies.tiernogalvan.eternaquest.service.interfaces.IPersonajeService;
import ies.tiernogalvan.eternaquest.service.interfaces.IRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class RankingServiceImpl implements IRankingService {

    private final EstadisticasJugadorRepository statsRepository;
    private final IPersonajeService personajeService;

    @Override
    public List<EstadisticasJugador> getRanking(int pagina, int tamanio) {
        return statsRepository.findAllByOrderByPuntosRankingDesc(PageRequest.of(pagina, tamanio));
    }

    @Override
    public EstadisticasJugador getMisEstadisticas(String email) {
        Personaje p = personajeService.getPersonajeEntity(email);
        return statsRepository.findByPersonajeId(p.getId())
                .orElseThrow(() -> new IllegalStateException("Estadísticas no encontradas"));
    }
}
