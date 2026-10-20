package com.heladeria.backend.producto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductoOpcionesService {

    private final ProductoRepository productoRepository;

    public ProductoOpcionesService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional
    public Producto configurar(Long productoId, ConfigurarOpcionesRequest request) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

        sincronizarVariantes(producto, request.variantes() != null ? request.variantes() : List.of());
        sincronizarGrupos(producto, request.gruposAdicionales() != null ? request.gruposAdicionales() : List.of());

        return productoRepository.save(producto);
    }

    private void sincronizarVariantes(Producto producto, List<ConfigurarOpcionesRequest.VarianteRequest> requests) {
        Map<Long, ProductoVariante> existentes = producto.getVariantes().stream()
                .collect(Collectors.toMap(ProductoVariante::getId, Function.identity()));

        List<ProductoVariante> resultado = new ArrayList<>();
        for (ConfigurarOpcionesRequest.VarianteRequest vr : requests) {
            ProductoVariante variante = vr.id() != null ? existentes.get(vr.id()) : null;
            boolean esNueva = variante == null;
            if (esNueva) {
                variante = new ProductoVariante();
                variante.setProducto(producto);
                variante.setStockActual(vr.stockActual());
            }
            variante.setNombre(vr.nombre());
            variante.setPrecio(vr.precio());
            variante.setCosto(vr.costo());
            variante.setSku(vr.sku());
            variante.setStockMinimo(vr.stockMinimo());
            variante.setActivo(vr.activo());
            resultado.add(variante);
        }

        producto.getVariantes().clear();
        producto.getVariantes().addAll(resultado);
    }

    private void sincronizarGrupos(Producto producto, List<ConfigurarOpcionesRequest.GrupoRequest> requests) {
        Map<Long, GrupoAdicionales> existentes = producto.getGruposAdicionales().stream()
                .collect(Collectors.toMap(GrupoAdicionales::getId, Function.identity()));

        List<GrupoAdicionales> resultado = new ArrayList<>();
        for (ConfigurarOpcionesRequest.GrupoRequest gr : requests) {
            GrupoAdicionales grupo = gr.id() != null ? existentes.get(gr.id()) : null;
            if (grupo == null) {
                grupo = new GrupoAdicionales();
                grupo.setProducto(producto);
            }
            grupo.setNombre(gr.nombre());
            grupo.setMinSeleccion(gr.minSeleccion());
            grupo.setMaxSeleccion(gr.maxSeleccion());
            sincronizarOpciones(grupo, gr.opciones());
            resultado.add(grupo);
        }

        producto.getGruposAdicionales().clear();
        producto.getGruposAdicionales().addAll(resultado);
    }

    private void sincronizarOpciones(GrupoAdicionales grupo, List<ConfigurarOpcionesRequest.GrupoRequest.AdicionalRequest> requests) {
        Map<Long, Adicional> existentes = grupo.getOpciones().stream()
                .collect(Collectors.toMap(Adicional::getId, Function.identity()));

        List<Adicional> resultado = new ArrayList<>();
        for (ConfigurarOpcionesRequest.GrupoRequest.AdicionalRequest ar : requests) {
            Adicional adicional = ar.id() != null ? existentes.get(ar.id()) : null;
            boolean esNueva = adicional == null;
            if (esNueva) {
                adicional = new Adicional();
                adicional.setGrupo(grupo);
                adicional.setStockActual(ar.stockActual() != null ? ar.stockActual() : java.math.BigDecimal.ZERO);
            }
            adicional.setNombre(ar.nombre());
            adicional.setPrecioAdicional(ar.precioAdicional());
            adicional.setCostoAdicional(ar.costoAdicional() != null ? ar.costoAdicional() : java.math.BigDecimal.ZERO);
            adicional.setControlaStock(ar.controlaStock());
            adicional.setStockMinimo(ar.stockMinimo() != null ? ar.stockMinimo() : java.math.BigDecimal.ZERO);
            adicional.setActivo(ar.activo());
            resultado.add(adicional);
        }

        grupo.getOpciones().clear();
        grupo.getOpciones().addAll(resultado);
    }
}
