package com.heladeria.backend.producto;

import com.heladeria.backend.insumo.Insumo;
import com.heladeria.backend.insumo.InsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductoRecetaService {

    private final ProductoRepository productoRepository;
    private final InsumoRepository insumoRepository;

    public ProductoRecetaService(ProductoRepository productoRepository, InsumoRepository insumoRepository) {
        this.productoRepository = productoRepository;
        this.insumoRepository = insumoRepository;
    }

    @Transactional
    public Producto configurar(Long productoId, ConfigurarRecetaRequest request) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

        producto.getReceta().clear();
        for (ConfigurarRecetaRequest.ItemRecetaRequest item : orVacia(request.recetaProducto())) {
            producto.getReceta().add(crearItem(producto, null, item));
        }

        Map<Long, ProductoVariante> variantesPorId = producto.getVariantes().stream()
                .collect(Collectors.toMap(ProductoVariante::getId, Function.identity()));

        for (ConfigurarRecetaRequest.RecetaVarianteRequest rv : orVacia(request.recetasVariantes())) {
            ProductoVariante variante = variantesPorId.get(rv.varianteId());
            if (variante == null) {
                continue; // la variante ya no existe; se ignora esa entrada
            }
            variante.getReceta().clear();
            for (ConfigurarRecetaRequest.ItemRecetaRequest item : orVacia(rv.items())) {
                variante.getReceta().add(crearItem(null, variante, item));
            }
        }

        return productoRepository.save(producto);
    }

    private RecetaItem crearItem(Producto producto, ProductoVariante variante, ConfigurarRecetaRequest.ItemRecetaRequest item) {
        Insumo insumo = insumoRepository.findById(item.insumoId())
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado: " + item.insumoId()));

        RecetaItem recetaItem = new RecetaItem();
        recetaItem.setProducto(producto);
        recetaItem.setVariante(variante);
        recetaItem.setInsumo(insumo);
        recetaItem.setCantidad(item.cantidad());
        return recetaItem;
    }

    private <T> List<T> orVacia(List<T> lista) {
        return lista != null ? lista : List.of();
    }
}
