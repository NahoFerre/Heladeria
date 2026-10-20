package com.heladeria.backend.gasto;

import com.heladeria.backend.common.Fechas;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService gastoService;

    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }

    @GetMapping
    public List<Gasto> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) String categoria) {
        return gastoService.historial(
                desde != null ? Fechas.inicioDelDia(desde) : null,
                hasta != null ? Fechas.finDelDia(hasta) : null,
                usuarioId, metodoPago, categoria);
    }

    @PostMapping
    public ResponseEntity<Gasto> crear(@Valid @RequestBody GastoRequest request, Authentication authentication) {
        Gasto gasto = gastoService.registrar(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(gasto);
    }
}
