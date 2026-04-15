package ies.tiernogalvan.eternaquest.service.impl;

import ies.tiernogalvan.eternaquest.model.entity.*;
import ies.tiernogalvan.eternaquest.model.enums.TipoObjeto;
import ies.tiernogalvan.eternaquest.repository.*;
import ies.tiernogalvan.eternaquest.service.interfaces.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class InventarioServiceImpl implements IInventarioService {

    private final InventarioItemRepository inventarioRepository;
    private final ObjetoRepository objetoRepository;
    private final PersonajeRepository personajeRepository;
    private final IPersonajeService personajeService;

    @Override
    public List<InventarioItem> getInventario(String email) {
        Personaje p = personajeService.getPersonajeEntity(email);
        return inventarioRepository.findByPersonajeId(p.getId());
    }

    @Override
    @Transactional
    public InventarioItem equipar(String email, Long objetoId) {
        Personaje p = personajeService.getPersonajeEntity(email);
        InventarioItem item = inventarioRepository
                .findByPersonajeIdAndObjetoId(p.getId(), objetoId)
                .orElseThrow(() -> new IllegalArgumentException("Objeto no encontrado en inventario"));

        TipoObjeto tipo = item.getObjeto().getTipo();
        if (tipo == TipoObjeto.CONSUMIBLE)
            throw new IllegalArgumentException("Los consumibles no se equipan, se usan");

        inventarioRepository.findByPersonajeIdAndEquipadoTrue(p.getId()).stream()
                .filter(i -> i.getObjeto().getTipo() == tipo && !i.getId().equals(item.getId()))
                .forEach(i -> {
                    aplicarBonus(p, i.getObjeto(), false);
                    i.setEquipado(false);
                    inventarioRepository.save(i);
                });

        item.setEquipado(true);
        aplicarBonus(p, item.getObjeto(), true);
        personajeRepository.save(p);
        return inventarioRepository.save(item);
    }

    @Override
    @Transactional
    public void desequipar(String email, Long objetoId) {
        Personaje p = personajeService.getPersonajeEntity(email);
        InventarioItem item = inventarioRepository
                .findByPersonajeIdAndObjetoId(p.getId(), objetoId)
                .orElseThrow(() -> new IllegalArgumentException("Objeto no encontrado"));
        if (!item.isEquipado()) throw new IllegalStateException("El objeto no está equipado");
        item.setEquipado(false);
        aplicarBonus(p, item.getObjeto(), false);
        personajeRepository.save(p);
        inventarioRepository.save(item);
    }

    @Override
    @Transactional
    public void usarConsumible(String email, Long objetoId) {
        Personaje p = personajeService.getPersonajeEntity(email);
        InventarioItem item = inventarioRepository
                .findByPersonajeIdAndObjetoId(p.getId(), objetoId)
                .orElseThrow(() -> new IllegalArgumentException("Objeto no encontrado"));
        if (item.getObjeto().getTipo() != TipoObjeto.CONSUMIBLE)
            throw new IllegalArgumentException("Solo se pueden usar consumibles de esta forma");
        aplicarEfectoConsumible(p, item.getObjeto());
        item.setCantidad(item.getCantidad() - 1);
        if (item.getCantidad() == 0) inventarioRepository.delete(item);
        else inventarioRepository.save(item);
        personajeRepository.save(p);
    }

    @Override
    @Transactional
    public InventarioItem comprarObjeto(String email, Long objetoId) {
        Personaje p = personajeService.getPersonajeEntity(email);
        Objeto objeto = objetoRepository.findById(objetoId)
                .orElseThrow(() -> new IllegalArgumentException("Objeto no encontrado"));
        if (p.getOro() < objeto.getPrecio())
            throw new IllegalStateException("Oro insuficiente");
        p.setOro(p.getOro() - objeto.getPrecio());
        personajeRepository.save(p);
        return inventarioRepository.findByPersonajeIdAndObjetoId(p.getId(), objetoId)
                .map(item -> { item.setCantidad(item.getCantidad() + 1); return inventarioRepository.save(item); })
                .orElseGet(() -> inventarioRepository.save(
                        InventarioItem.builder().personaje(p).objeto(objeto).cantidad(1).build()));
    }

    @Override
    @Transactional
    public void venderObjeto(String email, Long objetoId) {
        Personaje p = personajeService.getPersonajeEntity(email);
        InventarioItem item = inventarioRepository
                .findByPersonajeIdAndObjetoId(p.getId(), objetoId)
                .orElseThrow(() -> new IllegalArgumentException("Objeto no encontrado"));
        if (item.isEquipado()) throw new IllegalStateException("Desequipa el objeto antes de venderlo");
        int precioVenta = item.getObjeto().getPrecio() / 2;
        p.setOro(p.getOro() + precioVenta);
        item.setCantidad(item.getCantidad() - 1);
        if (item.getCantidad() == 0) inventarioRepository.delete(item);
        else inventarioRepository.save(item);
        personajeRepository.save(p);
    }

    private void aplicarBonus(Personaje p, Objeto o, boolean equipar) {
        if (o.getBonusStat() == null) return;
        int valor = equipar ? o.getValorBonus() : -o.getValorBonus();
        switch (o.getBonusStat().toLowerCase()) {
            case "ataque"    -> p.setAtaque(p.getAtaque() + valor);
            case "defensa"   -> p.setDefensa(p.getDefensa() + valor);
            case "velocidad" -> p.setVelocidad(p.getVelocidad() + valor);
            case "magia"     -> p.setMagia(p.getMagia() + valor);
            case "vida_max"  -> { p.setVidaMax(p.getVidaMax() + valor); p.setVidaActual(Math.min(p.getVidaActual(), p.getVidaMax())); }
        }
    }

    private void aplicarEfectoConsumible(Personaje p, Objeto o) {
        if (o.getBonusStat() == null) return;
        switch (o.getBonusStat().toLowerCase()) {
            case "vida"  -> p.setVidaActual(Math.min(p.getVidaMax(), p.getVidaActual() + o.getValorBonus()));
            case "mana"  -> p.setMana(Math.min(p.getManaMax(), p.getMana() + o.getValorBonus()));
        }
    }
}
