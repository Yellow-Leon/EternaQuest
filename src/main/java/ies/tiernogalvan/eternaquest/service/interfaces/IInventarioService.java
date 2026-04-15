package ies.tiernogalvan.eternaquest.service.interfaces;

import ies.tiernogalvan.eternaquest.model.entity.InventarioItem;
import java.util.List;

public interface IInventarioService {
    List<InventarioItem> getInventario(String email);
    InventarioItem equipar(String email, Long objetoId);
    void desequipar(String email, Long objetoId);
    void usarConsumible(String email, Long objetoId);
    InventarioItem comprarObjeto(String email, Long objetoId);
    void venderObjeto(String email, Long objetoId);
}
