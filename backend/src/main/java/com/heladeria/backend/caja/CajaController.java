package com.heladeria.backend.caja;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cajas")
public class CajaController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @GetMapping("/actual")
    public ResponseEntity<CajaActualResponse> actual() {
        return cajaService.obtenerActual()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/abrir")
    public ResponseEntity<CajaActualResponse> abrir(@Valid @RequestBody AbrirCajaRequest request, Authentication authentication) {
        CajaActualResponse caja = cajaService.abrir(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(caja);
    }

    @PostMapping("/cerrar")
    public CierreCajaResponse cerrar(@Valid @RequestBody CerrarCajaRequest request, Authentication authentication) {
        return cajaService.cerrar(request, authentication.getName());
    }

    @GetMapping
    public List<CajaHistorialResponse> historial() {
        return cajaService.historial();
    }

    @PostMapping("/movimientos")
    public MovimientoCajaResponse registrarMovimiento(@Valid @RequestBody MovimientoCajaRequest request, Authentication authentication) {
        return cajaService.registrarMovimiento(request, authentication.getName());
    }

    @GetMapping("/movimientos/actual")
    public List<MovimientoCajaResponse> movimientosDeLaCajaActual() {
        return cajaService.listarMovimientosCajaAbierta();
    }

    @GetMapping("/{id}/movimientos")
    public List<MovimientoCajaResponse> movimientos(@PathVariable Long id) {
        return cajaService.listarMovimientos(id);
    }
}
