package com.heladeria.backend.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final VentaRepository ventaRepository;

    public VentaController(VentaService ventaService, VentaRepository ventaRepository) {
        this.ventaService = ventaService;
        this.ventaRepository = ventaRepository;
    }

    @GetMapping
    public List<Venta> listar() {
        return ventaRepository.findAllByOrderByFechaDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtener(@PathVariable Long id) {
        return ventaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Venta> crear(@Valid @RequestBody VentaRequest request, Authentication authentication) {
        Venta venta = ventaService.crearVenta(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    @PutMapping("/{id}/marcar-impresa")
    public ResponseEntity<Venta> marcarImpresa(@PathVariable Long id) {
        return ventaRepository.findById(id)
                .map(venta -> {
                    venta.setImpresoEn(OffsetDateTime.now());
                    return ResponseEntity.ok(ventaRepository.save(venta));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/anular")
    public Venta anular(@PathVariable Long id, @Valid @RequestBody AnularVentaRequest request, Authentication authentication) {
        return ventaService.anularVenta(id, request.motivo(), authentication.getName());
    }

    public record AnularVentaRequest(@NotBlank String motivo) {
    }
}
