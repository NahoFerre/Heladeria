package com.heladeria.backend.producto;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos/{productoId}/opciones")
public class ProductoOpcionesController {

    private final ProductoOpcionesService opcionesService;

    public ProductoOpcionesController(ProductoOpcionesService opcionesService) {
        this.opcionesService = opcionesService;
    }

    @PutMapping
    public Producto configurar(@PathVariable Long productoId, @Valid @RequestBody ConfigurarOpcionesRequest request) {
        return opcionesService.configurar(productoId, request);
    }
}
