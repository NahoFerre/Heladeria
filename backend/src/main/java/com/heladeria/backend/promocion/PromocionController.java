package com.heladeria.backend.promocion;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")
public class PromocionController {

    private final PromocionService promocionService;

    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    @GetMapping
    public List<Promocion> listar(@RequestParam(defaultValue = "false") boolean soloActivas) {
        return promocionService.listar(soloActivas);
    }

    // Promociones activas y dentro de su rango de fechas ahora mismo: lo que Venta necesita para
    // calcular precios con descuento, visible también a CAJERO (a diferencia del resto de este controller)
    @GetMapping("/vigentes")
    public List<Promocion> vigentes() {
        return promocionService.vigentesAhora();
    }

    @PostMapping
    public ResponseEntity<Promocion> crear(@Valid @RequestBody PromocionRequest request, Authentication authentication) {
        Promocion promocion = promocionService.crear(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(promocion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Promocion> actualizar(@PathVariable Long id, @Valid @RequestBody PromocionRequest request,
                                                 Authentication authentication) {
        return promocionService.actualizar(id, request, authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        boolean eliminada = promocionService.eliminar(id, authentication.getName());
        return eliminada ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
