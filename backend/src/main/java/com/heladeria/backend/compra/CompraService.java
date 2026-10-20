package com.heladeria.backend.compra;

import com.heladeria.backend.auditoria.AuditoriaService;
import com.heladeria.backend.auditoria.TipoAccionAuditoria;
import com.heladeria.backend.common.ConfiguracionProductoException;
import com.heladeria.backend.insumo.Insumo;
import com.heladeria.backend.insumo.InsumoRepository;
import com.heladeria.backend.inventario.InventarioService;
import com.heladeria.backend.inventario.TipoMovimiento;
import com.heladeria.backend.producto.Producto;
import com.heladeria.backend.producto.ProductoRepository;
import com.heladeria.backend.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final InsumoRepository insumoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioService inventarioService;
    private final AuditoriaService auditoriaService;
    private final EstrategiaCosto estrategiaCosto;

    public CompraService(CompraRepository compraRepository, InsumoRepository insumoRepository,
                          ProductoRepository productoRepository, UsuarioRepository usuarioRepository,
                          InventarioService inventarioService, AuditoriaService auditoriaService,
                          @Value("${app.compras.estrategia-costo:COSTO_PROMEDIO}") EstrategiaCosto estrategiaCosto) {
        this.compraRepository = compraRepository;
        this.insumoRepository = insumoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inventarioService = inventarioService;
        this.auditoriaService = auditoriaService;
        this.estrategiaCosto = estrategiaCosto;
    }

    @Transactional
    public Compra registrarCompra(CompraRequest request, String username) {
        for (CompraRequest.ItemRequest item : request.items()) {
            boolean tieneInsumo = item.insumoId() != null;
            boolean tieneProducto = item.productoId() != null;
            if (tieneInsumo == tieneProducto) {
                throw new ConfiguracionProductoException("Cada ítem de la compra debe indicar exactamente un insumo o un producto");
            }
        }

        // Mismo patrón que VentaService.bloquearTodos: orden global fijo (insumos, luego productos,
        // cada grupo ordenado por id) para que dos compras/ventas simultáneas no se bloqueen cruzado
        Map<Long, Insumo> insumosPorId = bloquearTodos(
                request.items().stream().map(CompraRequest.ItemRequest::insumoId).filter(Objects::nonNull),
                id -> insumoRepository.buscarParaActualizarStock(id)
                        .orElseThrow(() -> new ConfiguracionProductoException("Insumo no encontrado: " + id))
        );
        Map<Long, Producto> productosPorId = bloquearTodos(
                request.items().stream().map(CompraRequest.ItemRequest::productoId).filter(Objects::nonNull),
                id -> productoRepository.buscarParaActualizarStock(id)
                        .orElseThrow(() -> new ConfiguracionProductoException("Producto no encontrado: " + id))
        );

        Compra compra = new Compra();
        compra.setProveedor(request.proveedor());
        compra.setNumeroFactura(request.numeroFactura());
        compra.setMetodoPago(request.metodoPago());
        compra.setObservaciones(request.observaciones());
        usuarioRepository.findByUsername(username).ifPresent(compra::setUsuario);
        compra.setUsuarioNombreMomento(username);

        BigDecimal total = BigDecimal.ZERO;
        for (CompraRequest.ItemRequest itemReq : request.items()) {
            CompraItem item = new CompraItem();
            item.setCompra(compra);
            item.setCantidad(itemReq.cantidad());
            item.setCostoUnitario(itemReq.costoUnitario());
            item.setSubtotal(itemReq.cantidad().multiply(itemReq.costoUnitario()));

            if (itemReq.insumoId() != null) {
                Insumo insumo = insumosPorId.get(itemReq.insumoId());
                insumo.setCostoUnitario(calcularNuevoCosto(insumo.getStockActual(), insumo.getCostoUnitario(), itemReq.cantidad(), itemReq.costoUnitario()));
                insumo.setStockActual(insumo.getStockActual().add(itemReq.cantidad()));
                insumoRepository.save(insumo);
                item.setInsumo(insumo);
                item.setNombreMomento(insumo.getNombre());
            } else {
                Producto producto = productosPorId.get(itemReq.productoId());
                producto.setCostoUnitario(calcularNuevoCosto(producto.getStockActual(), producto.getCostoUnitario(), itemReq.cantidad(), itemReq.costoUnitario()));
                producto.setStockActual(producto.getStockActual().add(itemReq.cantidad()));
                productoRepository.save(producto);
                item.setProducto(producto);
                item.setNombreMomento(producto.getNombre());
            }

            compra.getItems().add(item);
            total = total.add(item.getSubtotal());
        }
        compra.setTotal(total);

        Compra guardada = compraRepository.save(compra);

        for (CompraItem item : guardada.getItems()) {
            if (item.getProducto() != null) {
                inventarioService.guardarMovimiento(item.getProducto(), TipoMovimiento.ENTRADA, item.getCantidad(), "Compra #" + guardada.getId());
            }
        }

        auditoriaService.registrar(username, TipoAccionAuditoria.COMPRA_REGISTRADA, "COMPRA", guardada.getId(),
                "Proveedor: " + guardada.getProveedor() + ", total: " + guardada.getTotal());

        return guardada;
    }

    public List<Compra> historial(OffsetDateTime desde, OffsetDateTime hasta, Long usuarioId, String metodoPago) {
        return compraRepository.buscarFiltradas(desde, hasta, usuarioId, metodoPago);
    }

    private BigDecimal calcularNuevoCosto(BigDecimal stockActual, BigDecimal costoActual, BigDecimal cantidadComprada, BigDecimal costoCompra) {
        if (estrategiaCosto == EstrategiaCosto.ULTIMO_COSTO) {
            return costoCompra;
        }
        BigDecimal stockTotal = stockActual.add(cantidadComprada);
        if (stockTotal.compareTo(BigDecimal.ZERO) == 0) {
            return costoCompra;
        }
        BigDecimal valorActual = stockActual.multiply(costoActual);
        BigDecimal valorComprado = cantidadComprada.multiply(costoCompra);
        return valorActual.add(valorComprado).divide(stockTotal, 4, RoundingMode.HALF_UP);
    }

    private <T> Map<Long, T> bloquearTodos(Stream<Long> ids, Function<Long, T> bloquear) {
        Map<Long, T> resultado = new LinkedHashMap<>();
        ids.distinct().sorted().forEach(id -> resultado.put(id, bloquear.apply(id)));
        return resultado;
    }
}
