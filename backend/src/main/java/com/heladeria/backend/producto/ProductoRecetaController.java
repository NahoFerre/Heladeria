package com.heladeria.backend.producto;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos/{productoId}/receta")
public class ProductoRecetaController {

    private final ProductoRecetaService recetaService;

    public ProductoRecetaController(ProductoRecetaService recetaService) {
        this.recetaService = recetaService;
    }

    @PutMapping
    public Producto configurar(@PathVariable Long productoId, @Valid @RequestBody ConfigurarRecetaRequest request) {
        return recetaService.configurar(productoId, request);
    }
}
