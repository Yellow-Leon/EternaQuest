package ies.tiernogalvan.eternaquest.service.interfaces;

import ies.tiernogalvan.eternaquest.model.dto.request.AccionCombateRequest;
import ies.tiernogalvan.eternaquest.model.dto.response.CombateTurnoResponse;
import ies.tiernogalvan.eternaquest.model.entity.Personaje;

public interface ICombateService {
    CombateTurnoResponse iniciarCombate(String email, Long zonaId);
    CombateTurnoResponse ejecutarAccion(String email, AccionCombateRequest request);
    int calcularDanio(int ataque, int defensa);

    Personaje escalarANivel(Personaje invasor, int nivelObjetivo);
}
