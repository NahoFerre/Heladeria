package com.heladeria.backend.producto;

import com.heladeria.backend.auditoria.AuditoriaService;
import com.heladeria.backend.auditoria.TipoAccionAuditoria;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository repository;
    private final AuditoriaService auditoriaService;

    public ProductoController(ProductoRepository repository, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<Producto> listar(@RequestParam(defaultValue = "true") boolean soloActivos) {
        return soloActivos ? repository.findByActivoTrue() : repository.findAll();
    }

    @GetMapping("/categorias")
    public List<String> categorias() {
        return repository.buscarCategorias();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto, Authentication authentication) {
        producto.setId(null);
        Producto guardado = repository.save(producto);
        auditoriaService.registrar(authentication.getName(), TipoAccionAuditoria.PRODUCTO_CREADO, "PRODUCTO", guardado.getId(), guardado.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // stockActual no se toca acá: se ajusta vía movimientos_inventario (Fase 2) para mantener la auditoría de cambios
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @Valid @RequestBody Producto datos, Authentication authentication) {
        return repository.findById(id)
                .map(existente -> {
                    BigDecimal precioAnterior = existente.getPrecioVenta();
                    existente.setNombre(datos.getNombre());
                    existente.setCategoria(datos.getCategoria());
                    existente.setCostoUnitario(datos.getCostoUnitario());
                    existente.setPrecioVenta(datos.getPrecioVenta());
                    existente.setUnidadMedida(datos.getUnidadMedida());
                    existente.setStockMinimo(datos.getStockMinimo());
                    existente.setActivo(datos.isActivo());
                    Producto guardado = repository.save(existente);

                    boolean cambioPrecio = precioAnterior == null
                            ? datos.getPrecioVenta() != null
                            : precioAnterior.compareTo(datos.getPrecioVenta()) != 0;
                    if (cambioPrecio) {
                        auditoriaService.registrar(authentication.getName(), TipoAccionAuditoria.PRECIO_CAMBIADO, "PRODUCTO", guardado.getId(),
                                guardado.getNombre() + ": de " + precioAnterior + " a " + datos.getPrecioVenta());
                    } else {
                        auditoriaService.registrar(authentication.getName(), TipoAccionAuditoria.PRODUCTO_EDITADO, "PRODUCTO", guardado.getId(), guardado.getNombre());
                    }

                    return ResponseEntity.ok(guardado);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        return repository.findById(id)
                .map(producto -> {
                    producto.setActivo(false);
                    repository.save(producto);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
