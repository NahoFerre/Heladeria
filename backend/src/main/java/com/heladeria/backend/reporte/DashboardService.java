package com.heladeria.backend.reporte;

import com.heladeria.backend.caja.CajaActualResponse;
import com.heladeria.backend.caja.CajaService;
import com.heladeria.backend.insumo.Insumo;
import com.heladeria.backend.insumo.InsumoRepository;
import com.heladeria.backend.producto.Producto;
import com.heladeria.backend.producto.ProductoRepository;
import com.heladeria.backend.venta.VentaItemRepository;
import com.heladeria.backend.venta.VentaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private static final int DIAS_SIN_MOVIMIENTO = 30;

    private final VentaRepository ventaRepository;
    private final VentaItemRepository ventaItemRepository;
    private final ProductoRepository productoRepository;
    private final InsumoRepository insumoRepository;
    private final CajaService cajaService;

    public DashboardService(VentaRepository ventaRepository, VentaItemRepository ventaItemRepository,
                             ProductoRepository productoRepository, InsumoRepository insumoRepository,
                             CajaService cajaService) {
        this.ventaRepository = ventaRepository;
        this.ventaItemRepository = ventaItemRepository;
        this.productoRepository = productoRepository;
        this.insumoRepository = insumoRepository;
        this.cajaService = cajaService;
    }

    public DashboardResponse calcular() {
        OffsetDateTime ahora = OffsetDateTime.now();
        // Comparamos siempre "lo que va del período" contra el mismo tramo del período anterior, para que sea una comparación justa
        OffsetDateTime inicioHoy = ahora.truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime inicioSemana = inicioHoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        OffsetDateTime inicioMes = inicioHoy.withDayOfMonth(1);

        DashboardVentasResponse ventas = new DashboardVentasResponse(
                calcularPeriodo(inicioHoy, ahora, inicioHoy.minusDays(1), ahora.minusDays(1)),
                calcularPeriodo(inicioSemana, ahora, inicioSemana.minusWeeks(1), ahora.minusWeeks(1)),
                calcularPeriodo(inicioMes, ahora, inicioMes.minusMonths(1), ahora.minusMonths(1))
        );

        DashboardProductosResponse productos = calcularProductos(inicioMes, ahora);

        CajaActualResponse cajaActual = cajaService.obtenerActual().orElse(null);

        DashboardInventarioResponse inventario = new DashboardInventarioResponse(
                productoRepository.buscarStockBajo().stream().map(this::aStockBajoItem).toList(),
                insumoRepository.buscarStockBajo().stream().map(this::aStockBajoItem).toList(),
                productoRepository.buscarSinMovimientoDesde(ahora.minusDays(DIAS_SIN_MOVIMIENTO)).stream()
                        .map(p -> new ProductoSinMovimientoResponse(p.getId(), p.getNombre(), p.getCategoria()))
                        .toList()
        );

        return new DashboardResponse(ventas, productos, cajaActual, inventario);
    }

    private VentasPeriodoResponse calcularPeriodo(OffsetDateTime desde, OffsetDateTime hasta,
                                                    OffsetDateTime desdeAnterior, OffsetDateTime hastaAnterior) {
        ResumenVentasResponse actual = ventaRepository.resumenVentas(desde, hasta);
        ResumenVentasResponse anterior = ventaRepository.resumenVentas(desdeAnterior, hastaAnterior);
        return new VentasPeriodoResponse(actual.cantidadVentas(), actual.totalVendido(),
                variacion(actual.totalVendido(), anterior.totalVendido()));
    }

    // null cuando no hay base de comparación (período anterior en cero pero el actual sí vendió) — no es un % real
    private BigDecimal variacion(BigDecimal actual, BigDecimal anterior) {
        if (anterior.compareTo(BigDecimal.ZERO) == 0) {
            return actual.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : null;
        }
        return actual.subtract(anterior).multiply(BigDecimal.valueOf(100)).divide(anterior, 2, RoundingMode.HALF_UP);
    }

    private DashboardProductosResponse calcularProductos(OffsetDateTime desde, OffsetDateTime hasta) {
        List<ProductoRankingResponse> ranking = ventaItemRepository.rankingProductos(desde, hasta);
        if (ranking.isEmpty()) {
            return new DashboardProductosResponse(null, null, null, null);
        }
        ProductoRankingResponse mayorGanancia = ranking.get(0); // ya viene ordenado por ganancia desc
        ProductoRankingResponse masVendido = ranking.stream()
                .max(Comparator.comparing(ProductoRankingResponse::cantidadVendida)).orElseThrow();
        ProductoRankingResponse menorRotacion = ranking.stream()
                .min(Comparator.comparing(ProductoRankingResponse::cantidadVendida)).orElseThrow();
        ProductoRankingResponse mayorMargen = ranking.stream()
                .filter(r -> r.ingresos().compareTo(BigDecimal.ZERO) > 0)
                .max(Comparator.comparing(r -> r.ganancia().divide(r.ingresos(), 4, RoundingMode.HALF_UP)))
                .orElse(mayorGanancia);
        return new DashboardProductosResponse(masVendido, mayorGanancia, menorRotacion, mayorMargen);
    }

    private StockBajoItemResponse aStockBajoItem(Producto p) {
        return new StockBajoItemResponse(p.getId(), p.getNombre(), p.getStockActual(), p.getStockMinimo(), p.getUnidadMedida());
    }

    private StockBajoItemResponse aStockBajoItem(Insumo i) {
        return new StockBajoItemResponse(i.getId(), i.getNombre(), i.getStockActual(), i.getStockMinimo(), i.getUnidadMedida().name());
    }
}
