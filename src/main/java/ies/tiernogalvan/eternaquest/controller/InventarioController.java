package ies.tiernogalvan.eternaquest.controller;

import ies.tiernogalvan.eternaquest.model.entity.InventarioItem;
import ies.tiernogalvan.eternaquest.model.entity.Objeto;
import ies.tiernogalvan.eternaquest.repository.ObjetoRepository;
import ies.tiernogalvan.eternaquest.service.interfaces.IInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final IInventarioService inventarioService;
    private final ObjetoRepository objetoRepository;

    @GetMapping
    public ResponseEntity<List<InventarioItem>> getInventario(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(inventarioService.getInventario(user.getUsername()));
    }

    @PostMapping("/{objetoId}/equipar")
    public ResponseEntity<InventarioItem> equipar(@AuthenticationPrincipal UserDetails user,
                                                   @PathVariable Long objetoId) {
        return ResponseEntity.ok(inventarioService.equipar(user.getUsername(), objetoId));
    }

    @PostMapping("/{objetoId}/desequipar")
    public ResponseEntity<Void> desequipar(@AuthenticationPrincipal UserDetails user,
                                            @PathVariable Long objetoId) {
        inventarioService.desequipar(user.getUsername(), objetoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{objetoId}/usar")
    public ResponseEntity<Void> usar(@AuthenticationPrincipal UserDetails user,
                                      @PathVariable Long objetoId) {
        inventarioService.usarConsumible(user.getUsername(), objetoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{objetoId}/vender")
    public ResponseEntity<Void> vender(@AuthenticationPrincipal UserDetails user,
                                        @PathVariable Long objetoId) {
        inventarioService.venderObjeto(user.getUsername(), objetoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tienda")
    public ResponseEntity<List<Objeto>> getTienda() {
        return ResponseEntity.ok(objetoRepository.findAll());
    }

    @PostMapping("/tienda/{objetoId}/comprar")
    public ResponseEntity<InventarioItem> comprar(@AuthenticationPrincipal UserDetails user,
                                                   @PathVariable Long objetoId) {
        return ResponseEntity.ok(inventarioService.comprarObjeto(user.getUsername(), objetoId));
    }
}
