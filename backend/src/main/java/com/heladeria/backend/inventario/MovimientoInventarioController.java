package com.heladeria.backend.inventario;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos/{productoId}/movimientos")
public class MovimientoInventarioController {

    private final InventarioService inventarioService;
    private final MovimientoInventarioRepository movimientoRepository;

    public MovimientoInventarioController(InventarioService inventarioService,
                                           MovimientoInventarioRepository movimientoRepository) {
        this.inventarioService = inventarioService;
        this.movimientoRepository = movimientoRepository;
    }

    @GetMapping
    public List<MovimientoInventario> listar(@PathVariable Long productoId) {
        return movimientoRepository.findByProductoIdOrderByFechaDesc(productoId);
    }

    @PostMapping
    public MovimientoInventario registrar(@PathVariable Long productoId, @Valid @RequestBody MovimientoRequest request, Authentication authentication) {
        return inventarioService.registrarMovimiento(productoId, request.tipo(), request.cantidad(), request.motivo(), authentication.getName());
    }
}
