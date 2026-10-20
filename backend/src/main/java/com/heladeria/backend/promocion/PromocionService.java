package com.heladeria.backend.promocion;

import com.heladeria.backend.auditoria.AuditoriaService;
import com.heladeria.backend.auditoria.TipoAccionAuditoria;
import com.heladeria.backend.common.ConfiguracionProductoException;
import com.heladeria.backend.producto.Producto;
import com.heladeria.backend.producto.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PromocionService {

    private final PromocionRepository promocionRepository;
    private final ProductoRepository productoRepository;
    private final AuditoriaService auditoriaService;

    public PromocionService(PromocionRepository promocionRepository, ProductoRepository productoRepository,
                             AuditoriaService auditoriaService) {
        this.promocionRepository = promocionRepository;
        this.productoRepository = productoRepository;
        this.auditoriaService = auditoriaService;
    }

    public List<Promocion> listar(boolean soloActivas) {
        return soloActivas ? promocionRepository.findByActivoTrueOrderByNombre() : promocionRepository.findAllByOrderByNombre();
    }

    // Activas Y vigentes hoy (a diferencia de listar(true), que incluye activas fuera de su rango de fechas
    // — útil para la pantalla de administración, que también quiere ver promos programadas a futuro)
    public List<Promocion> vigentesAhora() {
        LocalDate hoy = LocalDate.now();
        return promocionRepository.findByActivoTrueOrderByNombre().stream().filter(p -> vigente(p, hoy)).toList();
    }

    @Transactional
    public Promocion crear(PromocionRequest request, String username) {
        Promocion promocion = new Promocion();
        aplicarDatos(promocion, request);
        Promocion guardada = promocionRepository.save(promocion);
        auditoriaService.registrar(username, TipoAccionAuditoria.PROMOCION_CREADA, "PROMOCION", guardada.getId(), detalle(guardada));
        return guardada;
    }

    @Transactional
    public Optional<Promocion> actualizar(Long id, PromocionRequest request, String username) {
        return promocionRepository.findById(id).map(existente -> {
            aplicarDatos(existente, request);
            Promocion guardada = promocionRepository.save(existente);
            auditoriaService.registrar(username, TipoAccionAuditoria.PROMOCION_EDITADA, "PROMOCION", guardada.getId(), detalle(guardada));
            return guardada;
        });
    }

    @Transactional
    public boolean eliminar(Long id, String username) {
        return promocionRepository.findById(id).map(promocion -> {
            promocion.setActivo(false);
            promocionRepository.save(promocion);
            auditoriaService.registrar(username, TipoAccionAuditoria.PROMOCION_ELIMINADA, "PROMOCION", id, promocion.getNombre());
            return true;
        }).orElse(false);
    }

    // La promoción vigente que da el mejor precio para este producto/cantidad, o vacío si ninguna aplica.
    // precioBase es el precio base ya resuelto por VentaService (el del producto, o el de la variante elegida).
    public Optional<Promocion> mejorPromocionPara(Producto producto, BigDecimal cantidad, BigDecimal precioBase) {
        return vigentesAhora().stream()
                .filter(p -> aplicaA(p, producto))
                .filter(p -> cantidad.compareTo(p.getCantidadMinima()) >= 0)
                .min(Comparator.comparing(p -> precioConPromocion(p, precioBase)));
    }

    public BigDecimal precioConPromocion(Promocion promocion, BigDecimal precioBase) {
        BigDecimal resultado = switch (promocion.getTipo()) {
            case PORCENTAJE -> precioBase.multiply(BigDecimal.valueOf(100).subtract(promocion.getValor()))
                    .divide(BigDecimal.valueOf(100));
            case MONTO_FIJO -> precioBase.subtract(promocion.getValor());
            case PRECIO_ESPECIAL -> promocion.getValor();
        };
        return resultado.max(BigDecimal.ZERO);
    }

    private boolean vigente(Promocion promocion, LocalDate hoy) {
        return (promocion.getFechaInicio() == null || !hoy.isBefore(promocion.getFechaInicio()))
                && (promocion.getFechaFin() == null || !hoy.isAfter(promocion.getFechaFin()));
    }

    // Sin producto ni categoría configurados, la promo aplica a todo el catálogo
    private boolean aplicaA(Promocion promocion, Producto producto) {
        if (promocion.getProducto() != null) {
            return promocion.getProducto().getId().equals(producto.getId());
        }
        if (promocion.getCategoria() != null && !promocion.getCategoria().isBlank()) {
            return promocion.getCategoria().equals(producto.getCategoria());
        }
        return true;
    }

    private void aplicarDatos(Promocion promocion, PromocionRequest request) {
        promocion.setNombre(request.nombre());
        promocion.setTipo(request.tipo());
        promocion.setValor(request.valor());
        promocion.setCategoria(request.categoria());
        promocion.setFechaInicio(request.fechaInicio());
        promocion.setFechaFin(request.fechaFin());
        promocion.setCantidadMinima(request.cantidadMinima() != null ? request.cantidadMinima() : BigDecimal.ONE);
        promocion.setActivo(request.activo());

        if (request.productoId() != null) {
            Producto producto = productoRepository.findById(request.productoId())
                    .orElseThrow(() -> new ConfiguracionProductoException("Producto no encontrado: " + request.productoId()));
            promocion.setProducto(producto);
        } else {
            promocion.setProducto(null);
        }
    }

    private String detalle(Promocion promocion) {
        return promocion.getNombre() + " (" + promocion.getTipo() + ": " + promocion.getValor() + ")";
    }
}
