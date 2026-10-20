package com.heladeria.backend.insumo;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insumos")
public class InsumoController {

    private final InsumoRepository repository;

    public InsumoController(InsumoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Insumo> listar(@RequestParam(defaultValue = "true") boolean soloActivos) {
        return soloActivos ? repository.findByActivoTrue() : repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Insumo> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Insumo> crear(@Valid @RequestBody Insumo insumo) {
        insumo.setId(null);
        Insumo guardado = repository.save(insumo);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // A diferencia de Producto, acá sí se deja editar stockActual directamente: todavía no existe
    // un sistema de movimientos para insumos (llegará con "Compras" en una fase posterior del roadmap)
    @PutMapping("/{id}")
    public ResponseEntity<Insumo> actualizar(@PathVariable Long id, @Valid @RequestBody Insumo datos) {
        return repository.findById(id)
                .map(existente -> {
                    existente.setNombre(datos.getNombre());
                    existente.setUnidadMedida(datos.getUnidadMedida());
                    existente.setCostoUnitario(datos.getCostoUnitario());
                    existente.setStockActual(datos.getStockActual());
                    existente.setStockMinimo(datos.getStockMinimo());
                    existente.setProveedor(datos.getProveedor());
                    existente.setActivo(datos.isActivo());
                    return ResponseEntity.ok(repository.save(existente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        return repository.findById(id)
                .map(insumo -> {
                    insumo.setActivo(false);
                    repository.save(insumo);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
