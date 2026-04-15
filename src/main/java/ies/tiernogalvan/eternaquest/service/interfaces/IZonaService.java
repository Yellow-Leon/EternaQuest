package ies.tiernogalvan.eternaquest.service.interfaces;

import ies.tiernogalvan.eternaquest.model.dto.response.ZonaResponse;
import java.util.List;

public interface IZonaService {
    List<ZonaResponse> listarZonas(String email);
    ZonaResponse entrarZona(String email, Long zonaId);
    void salirZona(String email);
}
