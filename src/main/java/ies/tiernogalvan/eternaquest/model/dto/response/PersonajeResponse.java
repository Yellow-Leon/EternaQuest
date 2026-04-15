package ies.tiernogalvan.eternaquest.model.dto.response;

import ies.tiernogalvan.eternaquest.model.entity.Personaje;
import lombok.Data;

@Data
public class PersonajeResponse {
    private Long id;
    private String nombre;
    private String clase;
    private int nivel;
    private int experiencia;
    private int expParaSiguienteNivel;
    private int oro;
    private int vidaActual;
    private int vidaMax;
    private int ataque;
    private int defensa;
    private int velocidad;
    private int magia;
    private int mana;
    private int manaMax;
    private Long zonaActualId;
    private String zonaActualNombre;

    public static PersonajeResponse from(Personaje p) {
        PersonajeResponse r = new PersonajeResponse();
        r.setId(p.getId());
        r.setNombre(p.getNombre());
        r.setClase(p.getClase().name());
        r.setNivel(p.getNivel());
        r.setExperiencia(p.getExperiencia());
        r.setExpParaSiguienteNivel(p.getExpParaSiguienteNivel());
        r.setOro(p.getOro());
        r.setVidaActual(p.getVidaActual());
        r.setVidaMax(p.getVidaMax());
        r.setAtaque(p.getAtaque());
        r.setDefensa(p.getDefensa());
        r.setVelocidad(p.getVelocidad());
        r.setMagia(p.getMagia());
        r.setMana(p.getMana());
        r.setManaMax(p.getManaMax());
        if (p.getZonaActual() != null) {
            r.setZonaActualId(p.getZonaActual().getId());
            r.setZonaActualNombre(p.getZonaActual().getNombre());
        }
        return r;
    }
}
