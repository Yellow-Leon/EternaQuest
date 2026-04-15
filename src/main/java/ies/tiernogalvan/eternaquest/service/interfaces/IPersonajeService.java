package ies.tiernogalvan.eternaquest.service.interfaces;

import ies.tiernogalvan.eternaquest.model.dto.request.CrearPersonajeRequest;
import ies.tiernogalvan.eternaquest.model.dto.response.PersonajeResponse;
import ies.tiernogalvan.eternaquest.model.entity.Personaje;

public interface IPersonajeService {
    PersonajeResponse crearPersonaje(String email, CrearPersonajeRequest request);
    PersonajeResponse getPersonaje(String email);
    Personaje getPersonajeEntity(String email);
    PersonajeResponse subirNivel(Personaje personaje);
    PersonajeResponse distribuirPuntos(String email, int vida, int ataque, int defensa, int velocidad, int magia);
}
