package com.heladeria.backend.merma;

import com.heladeria.backend.auditoria.AuditoriaService;
import com.heladeria.backend.auditoria.TipoAccionAuditoria;
import com.heladeria.backend.common.ConfiguracionProductoException;
import com.heladeria.backend.common.StockInsuficienteException;
import com.heladeria.backend.insumo.Insumo;
import com.heladeria.backend.insumo.InsumoRepository;
import com.heladeria.backend.inventario.InventarioService;
import com.heladeria.backend.inventario.TipoMovimiento;
import com.heladeria.backend.producto.Producto;
import com.heladeria.backend.producto.ProductoRepository;
import com.heladeria.backend.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class MermaService {

    private final MermaRepository mermaRepository;
    private final InsumoRepository insumoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioService inventarioService;
    private final AuditoriaService auditoriaService;

    public MermaService(MermaRepository mermaRepository, InsumoRepository insumoRepository,
                         ProductoRepository productoRepository, UsuarioRepository usuarioRepository,
                         InventarioService inventarioService, AuditoriaService auditoriaService) {
        this.mermaRepository = mermaRepository;
        this.insumoRepository = insumoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inventarioService = inventarioService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Merma registrarMerma(MermaRequest request, String username) {
        boolean tieneInsumo = request.insumoId() != null;
        boolean tieneProducto = request.productoId() != null;
        if (tieneInsumo == tieneProducto) {
            throw new ConfiguracionProductoException("La merma debe indicar exactamente un insumo o un producto");
        }

        Merma merma = new Merma();
        merma.setCantidad(request.cantidad());
        merma.setMotivo(request.motivo());
        merma.setObservacion(request.observacion());
        usuarioRepository.findByUsername(username).ifPresent(merma::setUsuario);
        merma.setUsuarioNombreMomento(username);

        if (tieneInsumo) {
            Insumo insumo = insumoRepository.buscarParaActualizarStock(request.insumoId())
                    .orElseThrow(() -> new ConfiguracionProductoException("Insumo no encontrado: " + request.insumoId()));
            descontarStock(insumo.getStockActual(), request.cantidad(), insumo.getNombre(), insumo::setStockActual);
            insumoRepository.save(insumo);
            merma.setInsumo(insumo);
            merma.setNombreMomento(insumo.getNombre());
            merma.setCostoUnitario(insumo.getCostoUnitario());
        } else {
            Producto producto = productoRepository.buscarParaActualizarStock(request.productoId())
                    .orElseThrow(() -> new ConfiguracionProductoException("Producto no encontrado: " + request.productoId()));
            descontarStock(producto.getStockActual(), request.cantidad(), producto.getNombre(), producto::setStockActual);
            productoRepository.save(producto);
            merma.setProducto(producto);
            merma.setNombreMomento(producto.getNombre());
            merma.setCostoUnitario(producto.getCostoUnitario());
        }
        merma.setCostoTotal(merma.getCostoUnitario().multiply(request.cantidad()));

        Merma guardada = mermaRepository.save(merma);

        if (guardada.getProducto() != null) {
            inventarioService.guardarMovimiento(guardada.getProducto(), TipoMovimiento.SALIDA, guardada.getCantidad(),
                    "Merma #" + guardada.getId() + ": " + guardada.getMotivo());
        }

        auditoriaService.registrar(username, TipoAccionAuditoria.MERMA_REGISTRADA, "MERMA", guardada.getId(),
                guardada.getNombreMomento() + " - " + guardada.getMotivo() + " (costo: " + guardada.getCostoTotal() + ")");

        return guardada;
    }

    public List<Merma> historial(OffsetDateTime desde, OffsetDateTime hasta, Long usuarioId, Long productoId) {
        return mermaRepository.buscarFiltradas(desde, hasta, usuarioId, productoId);
    }

    private void descontarStock(BigDecimal actual, BigDecimal cantidad, String nombre, java.util.function.Consumer<BigDecimal> setter) {
        BigDecimal restante = actual.subtract(cantidad);
        if (restante.compareTo(BigDecimal.ZERO) < 0) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para \"" + nombre + "\": disponible " + actual + ", solicitado " + cantidad);
        }
        setter.accept(restante);
    }
}
