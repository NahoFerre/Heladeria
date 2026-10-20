package com.heladeria.backend.merma;

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
@RequestMapping("/api/mermas")
public class MermaController {

    private final MermaService mermaService;

    public MermaController(MermaService mermaService) {
        this.mermaService = mermaService;
    }

    @GetMapping
    public List<Merma> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long productoId) {
        return mermaService.historial(
                desde != null ? Fechas.inicioDelDia(desde) : null,
                hasta != null ? Fechas.finDelDia(hasta) : null,
                usuarioId, productoId);
    }

    @PostMapping
    public ResponseEntity<Merma> crear(@Valid @RequestBody MermaRequest request, Authentication authentication) {
        Merma merma = mermaService.registrarMerma(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(merma);
    }
}
