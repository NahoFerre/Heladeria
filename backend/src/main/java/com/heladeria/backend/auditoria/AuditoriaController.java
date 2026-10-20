package com.heladeria.backend.auditoria;

import com.heladeria.backend.common.Fechas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// Ningún endpoint de escritura existe a propósito: el registro de auditoría solo se crea internamente
// desde otros servicios (AuditoriaService.registrar), nunca desde el frontend.
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public Page<RegistroAuditoria> listar(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                           @RequestParam(required = false) Long usuarioId) {
        return auditoriaService.listar(
                desde != null ? Fechas.inicioDelDia(desde) : null,
                hasta != null ? Fechas.finDelDia(hasta) : null,
                usuarioId, PageRequest.of(page, size));
    }
}
