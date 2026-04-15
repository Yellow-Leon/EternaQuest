package ies.tiernogalvan.eternaquest.service.interfaces;

import ies.tiernogalvan.eternaquest.model.entity.EstadisticasJugador;
import java.util.List;

public interface IRankingService {
    List<EstadisticasJugador> getRanking(int pagina, int tamanio);
    EstadisticasJugador getMisEstadisticas(String email);
}
