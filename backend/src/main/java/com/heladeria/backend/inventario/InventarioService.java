package com.heladeria.backend.inventario;

import com.heladeria.backend.auditoria.AuditoriaService;
import com.heladeria.backend.auditoria.TipoAccionAuditoria;
import com.heladeria.backend.common.StockInsuficienteException;
import com.heladeria.backend.producto.Producto;
import com.heladeria.backend.producto.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final AuditoriaService auditoriaService;

    public InventarioService(ProductoRepository productoRepository,
                              MovimientoInventarioRepository movimientoRepository,
                              AuditoriaService auditoriaService) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public MovimientoInventario registrarMovimiento(Long productoId, TipoMovimiento tipo, BigDecimal cantidad, String motivo, String username) {
        Producto producto = bloquearProducto(productoId);
        aplicarCambioStock(producto, tipo, cantidad);
        productoRepository.save(producto);
        MovimientoInventario movimiento = guardarMovimiento(producto, tipo, cantidad, motivo);
        auditoriaService.registrar(username, TipoAccionAuditoria.STOCK_AJUSTADO, "PRODUCTO", producto.getId(),
                producto.getNombre() + ": " + tipo + " " + cantidad + (motivo != null && !motivo.isBlank() ? " (" + motivo + ")" : ""));
        return movimiento;
    }

    // Bloqueo pesimista sobre el producto; usado también por VentaService dentro de su propia transacción
    public Producto bloquearProducto(Long productoId) {
        return productoRepository.buscarParaActualizarStock(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
    }

    // Descuenta stock sobre un producto ya bloqueado por el llamador (no vuelve a hacer lock ni fetch)
    public void descontarStockVenta(Producto producto, BigDecimal cantidad) {
        aplicarCambioStock(producto, TipoMovimiento.SALIDA, cantidad);
    }

    // Reversa simétrica de descontarStockVenta, para anular una venta (producto ya bloqueado por el llamador)
    public void revertirStockVenta(Producto producto, BigDecimal cantidad) {
        producto.setStockActual(producto.getStockActual().add(cantidad));
    }

    public MovimientoInventario guardarMovimiento(Producto producto, TipoMovimiento tipo, BigDecimal cantidad, String motivo) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        return movimientoRepository.save(movimiento);
    }

    private void aplicarCambioStock(Producto producto, TipoMovimiento tipo, BigDecimal cantidad) {
        switch (tipo) {
            case ENTRADA -> producto.setStockActual(producto.getStockActual().add(cantidad));
            case SALIDA -> descontarStock(producto, cantidad);
            case AJUSTE -> producto.setStockActual(cantidad); // AJUSTE fija el stock al valor absoluto indicado
        }
    }

    private void descontarStock(Producto producto, BigDecimal cantidad) {
        BigDecimal restante = producto.getStockActual().subtract(cantidad);
        if (restante.compareTo(BigDecimal.ZERO) < 0) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para \"" + producto.getNombre() + "\": disponible " + producto.getStockActual() + ", solicitado " + cantidad);
        }
        producto.setStockActual(restante);
    }
}
