package ies.tiernogalvan.eternaquest.controller;

import ies.tiernogalvan.eternaquest.model.entity.*;
import ies.tiernogalvan.eternaquest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final EnemigoRepository enemigoRepository;
    private final ObjetoRepository objetoRepository;
    private final ZonaRepository zonaRepository;
    private final HabilidadRepository habilidadRepository;

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> getUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/enemigos")
    public ResponseEntity<List<Enemigo>> getEnemigos() { return ResponseEntity.ok(enemigoRepository.findAll()); }

    @PostMapping("/enemigos")
    public ResponseEntity<Enemigo> crearEnemigo(@RequestBody Enemigo enemigo) {
        return ResponseEntity.ok(enemigoRepository.save(enemigo));
    }

    @PutMapping("/enemigos/{id}")
    public ResponseEntity<Enemigo> actualizarEnemigo(@PathVariable Long id, @RequestBody Enemigo datos) {
        return enemigoRepository.findById(id).map(e -> {
            e.setNombre(datos.getNombre()); e.setVida(datos.getVida());
            e.setAtaque(datos.getAtaque()); e.setDefensa(datos.getDefensa());
            e.setExpRecompensa(datos.getExpRecompensa()); e.setOroRecompensa(datos.getOroRecompensa());
            return ResponseEntity.ok(enemigoRepository.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/enemigos/{id}")
    public ResponseEntity<Void> eliminarEnemigo(@PathVariable Long id) {
        enemigoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/objetos")
    public ResponseEntity<Objeto> crearObjeto(@RequestBody Objeto objeto) {
        return ResponseEntity.ok(objetoRepository.save(objeto));
    }

    @DeleteMapping("/objetos/{id}")
    public ResponseEntity<Void> eliminarObjeto(@PathVariable Long id) {
        objetoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zonas")
    public ResponseEntity<List<Zona>> getZonas() { return ResponseEntity.ok(zonaRepository.findAll()); }

    @PostMapping("/zonas")
    public ResponseEntity<Zona> crearZona(@RequestBody Zona zona) {
        return ResponseEntity.ok(zonaRepository.save(zona));
    }

    @DeleteMapping("/zonas/{id}")
    public ResponseEntity<Void> eliminarZona(@PathVariable Long id) {
        zonaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/habilidades")
    public ResponseEntity<Habilidad> crearHabilidad(@RequestBody Habilidad hab) {
        return ResponseEntity.ok(habilidadRepository.save(hab));
    }

    @DeleteMapping("/habilidades/{id}")
    public ResponseEntity<Void> eliminarHabilidad(@PathVariable Long id) {
        habilidadRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
