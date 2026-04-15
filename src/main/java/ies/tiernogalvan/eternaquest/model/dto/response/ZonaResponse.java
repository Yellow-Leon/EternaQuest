package ies.tiernogalvan.eternaquest.model.dto.response;

import ies.tiernogalvan.eternaquest.model.entity.Zona;
import lombok.Data;

@Data
public class ZonaResponse {
    private Long id;
    private String nombre;
    private int nivelRequerido;
    private String descripcion;
    private boolean accesible;

    public static ZonaResponse from(Zona z, int nivelPersonaje) {
        ZonaResponse r = new ZonaResponse();
        r.setId(z.getId());
        r.setNombre(z.getNombre());
        r.setNivelRequerido(z.getNivelRequerido());
        r.setDescripcion(z.getDescripcion());
        r.setAccesible(nivelPersonaje >= z.getNivelRequerido());
        return r;
    }
}
