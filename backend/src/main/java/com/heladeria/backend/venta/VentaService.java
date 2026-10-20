package com.heladeria.backend.venta;

import com.heladeria.backend.auditoria.AuditoriaService;
import com.heladeria.backend.auditoria.TipoAccionAuditoria;
import com.heladeria.backend.caja.Caja;
import com.heladeria.backend.caja.CajaRepository;
import com.heladeria.backend.caja.EstadoCaja;
import com.heladeria.backend.common.CajaException;
import com.heladeria.backend.common.ConfiguracionProductoException;
import com.heladeria.backend.common.StockInsuficienteException;
import com.heladeria.backend.insumo.Insumo;
import com.heladeria.backend.insumo.InsumoRepository;
import com.heladeria.backend.inventario.InventarioService;
import com.heladeria.backend.inventario.TipoMovimiento;
import com.heladeria.backend.producto.Adicional;
import com.heladeria.backend.producto.AdicionalRepository;
import com.heladeria.backend.producto.Producto;
import com.heladeria.backend.producto.ProductoRepository;
import com.heladeria.backend.producto.ProductoVariante;
import com.heladeria.backend.producto.ProductoVarianteRepository;
import com.heladeria.backend.producto.RecetaItem;
import com.heladeria.backend.promocion.Promocion;
import com.heladeria.backend.promocion.PromocionService;
import com.heladeria.backend.common.VentaException;
import com.heladeria.backend.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ProductoVarianteRepository productoVarianteRepository;
    private final AdicionalRepository adicionalRepository;
    private final InsumoRepository insumoRepository;
    private final InventarioService inventarioService;
    private final UsuarioRepository usuarioRepository;
    private final CajaRepository cajaRepository;
    private final AuditoriaService auditoriaService;
    private final PromocionService promocionService;

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository,
                         ProductoVarianteRepository productoVarianteRepository, AdicionalRepository adicionalRepository,
                         InsumoRepository insumoRepository, InventarioService inventarioService,
                         UsuarioRepository usuarioRepository, CajaRepository cajaRepository,
                         AuditoriaService auditoriaService, PromocionService promocionService) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.productoVarianteRepository = productoVarianteRepository;
        this.adicionalRepository = adicionalRepository;
        this.insumoRepository = insumoRepository;
        this.inventarioService = inventarioService;
        this.usuarioRepository = usuarioRepository;
        this.cajaRepository = cajaRepository;
        this.auditoriaService = auditoriaService;
        this.promocionService = promocionService;
    }

    @Transactional
    public Venta crearVenta(VentaRequest request, String username) {
        Caja cajaAbierta = cajaRepository.findByEstado(EstadoCaja.ABIERTA)
                .orElseThrow(() -> new CajaException("No hay una caja abierta. Abrí la caja antes de registrar ventas."));

        Venta venta = new Venta();
        venta.setCaja(cajaAbierta);
        venta.setMetodoPago(request.metodoPago() != null && !request.metodoPago().isBlank() ? request.metodoPago() : "EFECTIVO");
        usuarioRepository.findByUsername(username).ifPresent(venta::setUsuario);

        // Se bloquea todo en un orden global fijo (productos, variantes, adicionales, insumos —
        // cada grupo ordenado por id) para que dos ventas simultáneas nunca se bloqueen en orden cruzado
        Map<Long, Producto> productosPorId = bloquearTodos(
                request.items().stream().map(VentaRequest.ItemRequest::productoId),
                inventarioService::bloquearProducto
        );
        Map<Long, ProductoVariante> variantesPorId = bloquearTodos(
                request.items().stream().map(VentaRequest.ItemRequest::varianteId).filter(Objects::nonNull),
                id -> productoVarianteRepository.buscarParaActualizarStock(id)
                        .orElseThrow(() -> new ConfiguracionProductoException("Variante no encontrada: " + id))
        );
        Map<Long, Adicional> adicionalesPorId = bloquearTodos(
                request.items().stream().flatMap(this::idsAdicionales),
                id -> adicionalRepository.buscarParaActualizarStock(id)
                        .orElseThrow(() -> new ConfiguracionProductoException("Adicional no encontrado: " + id))
        );
        Map<Long, Insumo> insumosPorId = bloquearTodos(
                request.items().stream().flatMap(itemReq -> insumosDe(itemReq, productosPorId, variantesPorId)),
                id -> insumoRepository.buscarParaActualizarStock(id)
                        .orElseThrow(() -> new ConfiguracionProductoException("Insumo no encontrado: " + id))
        );

        BigDecimal total = BigDecimal.ZERO;
        for (VentaRequest.ItemRequest itemReq : request.items()) {
            Producto producto = productosPorId.get(itemReq.productoId());
            ProductoVariante variante = resolverVariante(producto, itemReq.varianteId(), variantesPorId);

            BigDecimal precioBase = variante != null ? variante.getPrecio() : producto.getPrecioVenta();
            List<RecetaItem> receta = variante != null ? variante.getReceta() : producto.getReceta();

            // La promoción (si aplica alguna) se resuelve acá, sobre el precio base ya elegido (el de la
            // variante si corresponde) — nunca sobre lo que mande el cliente, que no envía precios
            Promocion promocion = promocionService.mejorPromocionPara(producto, itemReq.cantidad(), precioBase).orElse(null);
            BigDecimal precioConDescuento = promocion != null ? promocionService.precioConPromocion(promocion, precioBase) : precioBase;

            VentaItem item = new VentaItem();
            item.setVenta(venta);
            item.setProducto(producto);
            item.setCantidad(itemReq.cantidad());
            if (variante != null) {
                item.setVarianteId(variante.getId());
                item.setVarianteNombreMomento(variante.getNombre());
            }
            if (promocion != null) {
                item.setPromocionId(promocion.getId());
                item.setPromocionNombreMomento(promocion.getNombre());
            }

            BigDecimal costoBase;
            if (!receta.isEmpty()) {
                costoBase = consumirReceta(item, receta, itemReq.cantidad(), insumosPorId);
            } else if (variante != null) {
                costoBase = variante.getCosto();
                descontarStock(variante.getStockActual(), itemReq.cantidad(), variante.getNombre(), variante::setStockActual);
                productoVarianteRepository.save(variante);
            } else {
                costoBase = producto.getCostoUnitario();
                inventarioService.descontarStockVenta(producto, itemReq.cantidad());
                productoRepository.save(producto);
            }

            item.setPrecioUnitarioMomento(precioConDescuento);
            item.setCostoUnitarioMomento(costoBase);

            BigDecimal totalAdicionalesUnitario = agregarAdicionales(item, producto, itemReq, adicionalesPorId);

            venta.getItems().add(item);

            BigDecimal precioUnitarioFinal = precioConDescuento.add(totalAdicionalesUnitario);
            total = total.add(precioUnitarioFinal.multiply(itemReq.cantidad()));
        }
        venta.setTotal(total);

        Venta guardada = ventaRepository.save(venta);

        // El historial de movimientos de stock (auditoría) sigue existiendo solo para productos simples;
        // el stock de variantes/adicionales/insumos ya quedó descontado arriba, sin un registro de movimiento aparte por ahora
        for (VentaItem item : guardada.getItems()) {
            if (item.getVarianteId() == null) {
                inventarioService.guardarMovimiento(item.getProducto(), TipoMovimiento.SALIDA, item.getCantidad(), "Venta #" + guardada.getId());
            }
        }

        auditoriaService.registrar(username, TipoAccionAuditoria.VENTA_CREADA, "VENTA", guardada.getId(), "Total: " + guardada.getTotal());

        return guardada;
    }

    @Transactional
    public Venta anularVenta(Long ventaId, String motivo, String username) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaException("Venta no encontrada: " + ventaId));
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new VentaException("La venta #" + ventaId + " ya fue anulada");
        }

        // Se bloquea todo lo que la venta original tocó, usando los datos ya congelados en la propia
        // venta (no el catálogo actual) y respetando el mismo orden global fijo que usa crearVenta
        Map<Long, Producto> productosPorId = bloquearTodos(
                venta.getItems().stream().map(item -> item.getProducto().getId()),
                inventarioService::bloquearProducto
        );
        Map<Long, ProductoVariante> variantesPorId = bloquearTodos(
                venta.getItems().stream().map(VentaItem::getVarianteId).filter(Objects::nonNull),
                id -> productoVarianteRepository.buscarParaActualizarStock(id)
                        .orElseThrow(() -> new ConfiguracionProductoException("Variante no encontrada: " + id))
        );
        Map<Long, Adicional> adicionalesPorId = bloquearTodos(
                venta.getItems().stream().flatMap(item -> item.getAdicionales().stream()).map(VentaItemAdicional::getAdicionalId),
                id -> adicionalRepository.buscarParaActualizarStock(id)
                        .orElseThrow(() -> new ConfiguracionProductoException("Adicional no encontrado: " + id))
        );
        Map<Long, Insumo> insumosPorId = bloquearTodos(
                venta.getItems().stream().flatMap(item -> item.getInsumosConsumidos().stream()).map(VentaItemInsumo::getInsumoId),
                id -> insumoRepository.buscarParaActualizarStock(id)
                        .orElseThrow(() -> new ConfiguracionProductoException("Insumo no encontrado: " + id))
        );

        for (VentaItem item : venta.getItems()) {
            if (!item.getInsumosConsumidos().isEmpty()) {
                for (VentaItemInsumo consumo : item.getInsumosConsumidos()) {
                    Insumo insumo = insumosPorId.get(consumo.getInsumoId());
                    insumo.setStockActual(insumo.getStockActual().add(consumo.getCantidadConsumida()));
                    insumoRepository.save(insumo);
                }
            } else if (item.getVarianteId() != null) {
                ProductoVariante variante = variantesPorId.get(item.getVarianteId());
                variante.setStockActual(variante.getStockActual().add(item.getCantidad()));
                productoVarianteRepository.save(variante);
            } else {
                Producto producto = productosPorId.get(item.getProducto().getId());
                inventarioService.revertirStockVenta(producto, item.getCantidad());
                productoRepository.save(producto);
                inventarioService.guardarMovimiento(producto, TipoMovimiento.ENTRADA, item.getCantidad(), "Anulación de venta #" + ventaId);
            }

            for (VentaItemAdicional itemAdicional : item.getAdicionales()) {
                if (itemAdicional.getCantidadStockDescontada() != null) {
                    Adicional adicional = adicionalesPorId.get(itemAdicional.getAdicionalId());
                    adicional.setStockActual(adicional.getStockActual().add(itemAdicional.getCantidadStockDescontada()));
                    adicionalRepository.save(adicional);
                }
            }
        }

        venta.setEstado(EstadoVenta.ANULADA);
        venta.setAnuladaEn(OffsetDateTime.now());
        usuarioRepository.findByUsername(username).ifPresent(venta::setAnuladaPor);
        venta.setMotivoAnulacion(motivo);

        Venta anulada = ventaRepository.save(venta);
        auditoriaService.registrar(username, TipoAccionAuditoria.VENTA_ANULADA, "VENTA", anulada.getId(), motivo);
        return anulada;
    }

    private Stream<Long> idsAdicionales(VentaRequest.ItemRequest item) {
        return item.adicionalIds() != null ? item.adicionalIds().stream() : Stream.empty();
    }

    // Recolecta los insumos de la receta que aplicaría a este item, sin validar todavía (la validación
    // real ocurre en el loop principal); si la variante indicada no existe, simplemente no aporta insumos.
    private Stream<Long> insumosDe(VentaRequest.ItemRequest itemReq, Map<Long, Producto> productosPorId, Map<Long, ProductoVariante> variantesPorId) {
        Producto producto = productosPorId.get(itemReq.productoId());
        if (producto == null) {
            return Stream.empty();
        }
        List<RecetaItem> receta;
        if (itemReq.varianteId() != null) {
            ProductoVariante variante = variantesPorId.get(itemReq.varianteId());
            receta = variante != null ? variante.getReceta() : List.of();
        } else {
            receta = producto.getReceta();
        }
        return receta.stream().map(ri -> ri.getInsumo().getId());
    }

    private BigDecimal consumirReceta(VentaItem item, List<RecetaItem> receta, BigDecimal cantidadVendida, Map<Long, Insumo> insumosPorId) {
        BigDecimal costoBase = BigDecimal.ZERO;
        for (RecetaItem recetaItem : receta) {
            Insumo insumo = insumosPorId.get(recetaItem.getInsumo().getId());
            BigDecimal cantidadNecesaria = recetaItem.getCantidad().multiply(cantidadVendida);
            descontarStock(insumo.getStockActual(), cantidadNecesaria, insumo.getNombre(), insumo::setStockActual);
            insumoRepository.save(insumo);
            costoBase = costoBase.add(recetaItem.getCantidad().multiply(insumo.getCostoUnitario()));

            // Se congela lo efectivamente consumido (ya multiplicado por lo vendido) para poder revertirlo
            // exactamente si la venta se anula, sin depender de una receta que podría cambiar después
            VentaItemInsumo consumo = new VentaItemInsumo();
            consumo.setVentaItem(item);
            consumo.setInsumoId(insumo.getId());
            consumo.setNombreMomento(insumo.getNombre());
            consumo.setCantidadConsumida(cantidadNecesaria);
            item.getInsumosConsumidos().add(consumo);
        }
        return costoBase;
    }

    private <T> Map<Long, T> bloquearTodos(Stream<Long> ids, java.util.function.Function<Long, T> bloquear) {
        Map<Long, T> resultado = new LinkedHashMap<>();
        ids.distinct().sorted().forEach(id -> resultado.put(id, bloquear.apply(id)));
        return resultado;
    }

    private ProductoVariante resolverVariante(Producto producto, Long varianteId, Map<Long, ProductoVariante> variantesPorId) {
        boolean tieneVariantesActivas = producto.getVariantes().stream().anyMatch(ProductoVariante::isActivo);

        if (tieneVariantesActivas && varianteId == null) {
            throw new ConfiguracionProductoException("El producto \"" + producto.getNombre() + "\" requiere elegir una variante");
        }
        if (!tieneVariantesActivas && varianteId != null) {
            throw new ConfiguracionProductoException("El producto \"" + producto.getNombre() + "\" no tiene variantes configuradas");
        }
        if (varianteId == null) {
            return null;
        }

        ProductoVariante variante = variantesPorId.get(varianteId);
        if (variante == null || !variante.getProducto().getId().equals(producto.getId()) || !variante.isActivo()) {
            throw new ConfiguracionProductoException("La variante seleccionada no es válida para \"" + producto.getNombre() + "\"");
        }
        return variante;
    }

    private BigDecimal agregarAdicionales(VentaItem item, Producto producto, VentaRequest.ItemRequest itemReq, Map<Long, Adicional> adicionalesPorId) {
        BigDecimal totalAdicionalesUnitario = BigDecimal.ZERO;
        List<Long> ids = itemReq.adicionalIds() != null ? itemReq.adicionalIds() : List.of();

        for (Long adicionalId : ids) {
            Adicional adicional = adicionalesPorId.get(adicionalId);
            if (adicional == null || !adicional.getGrupo().getProducto().getId().equals(producto.getId()) || !adicional.isActivo()) {
                throw new ConfiguracionProductoException("El adicional seleccionado no es válido para \"" + producto.getNombre() + "\"");
            }
            VentaItemAdicional itemAdicional = new VentaItemAdicional();
            itemAdicional.setVentaItem(item);
            itemAdicional.setAdicionalId(adicional.getId());
            itemAdicional.setNombreMomento(adicional.getNombre());
            itemAdicional.setPrecioAdicionalMomento(adicional.getPrecioAdicional());
            itemAdicional.setCostoAdicionalMomento(adicional.getCostoAdicional());

            if (adicional.isControlaStock()) {
                descontarStock(adicional.getStockActual(), itemReq.cantidad(), adicional.getNombre(), adicional::setStockActual);
                adicionalRepository.save(adicional);
                itemAdicional.setCantidadStockDescontada(itemReq.cantidad());
            }

            item.getAdicionales().add(itemAdicional);

            totalAdicionalesUnitario = totalAdicionalesUnitario.add(adicional.getPrecioAdicional());
        }

        return totalAdicionalesUnitario;
    }

    private void descontarStock(BigDecimal actual, BigDecimal cantidad, String nombre, Consumer<BigDecimal> setter) {
        BigDecimal restante = actual.subtract(cantidad);
        if (restante.compareTo(BigDecimal.ZERO) < 0) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para \"" + nombre + "\": disponible " + actual + ", solicitado " + cantidad);
        }
        setter.accept(restante);
    }
}
