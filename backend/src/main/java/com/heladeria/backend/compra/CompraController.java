package com.heladeria.backend.compra;

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
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;
    private final CompraRepository compraRepository;

    public CompraController(CompraService compraService, CompraRepository compraRepository) {
        this.compraService = compraService;
        this.compraRepository = compraRepository;
    }

    @GetMapping
    public List<Compra> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String metodoPago) {
        return compraService.historial(
                desde != null ? Fechas.inicioDelDia(desde) : null,
                hasta != null ? Fechas.finDelDia(hasta) : null,
                usuarioId, metodoPago);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Compra> obtener(@PathVariable Long id) {
        return compraRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Compra> crear(@Valid @RequestBody CompraRequest request, Authentication authentication) {
        Compra compra = compraService.registrarCompra(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(compra);
    }
}
