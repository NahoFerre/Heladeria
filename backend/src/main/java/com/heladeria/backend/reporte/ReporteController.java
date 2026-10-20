package com.heladeria.backend.reporte;

import com.heladeria.backend.inventario.MovimientoInventarioRepository;
import com.heladeria.backend.producto.Producto;
import com.heladeria.backend.producto.ProductoRepository;
import com.heladeria.backend.venta.VentaItemRepository;
import com.heladeria.backend.venta.VentaRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final VentaItemRepository ventaItemRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final ReporteGananciaService reporteGananciaService;
    private final DashboardService dashboardService;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public ReporteController(VentaItemRepository ventaItemRepository, ProductoRepository productoRepository,
                              VentaRepository ventaRepository, ReporteGananciaService reporteGananciaService,
                              DashboardService dashboardService, MovimientoInventarioRepository movimientoInventarioRepository) {
        this.ventaItemRepository = ventaItemRepository;
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.reporteGananciaService = reporteGananciaService;
        this.dashboardService = dashboardService;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    // Dashboard avanzado (Inicio para ADMIN): ventas hoy/semana/mes con variación, productos destacados
    // del mes, caja actual e inventario en alerta. Sin parámetros de fecha, siempre "a este momento".
    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return dashboardService.calcular();
    }

    // Sin parámetros, por defecto reporta el mes en curso
    @GetMapping("/ganancias")
    public ReporteGananciaResponse ganancias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate hastaFinal = hasta != null ? hasta : LocalDate.now();
        LocalDate desdeFinal = desde != null ? desde : hastaFinal.withDayOfMonth(1);
        return reporteGananciaService.calcular(inicioDelDia(desdeFinal), finDelDia(hastaFinal));
    }

    @GetMapping("/productos-mas-rentables")
    public List<ProductoRankingResponse> productosMasRentables(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate hastaFinal = hasta != null ? hasta : LocalDate.now();
        LocalDate desdeFinal = desde != null ? desde : hastaFinal.withDayOfMonth(1);
        return ventaItemRepository.rankingProductos(inicioDelDia(desdeFinal), finDelDia(hastaFinal));
    }

    @GetMapping("/categorias-mas-rentables")
    public List<CategoriaRankingResponse> categoriasMasRentables(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate hastaFinal = hasta != null ? hasta : LocalDate.now();
        LocalDate desdeFinal = desde != null ? desde : hastaFinal.withDayOfMonth(1);
        return ventaItemRepository.rankingCategorias(inicioDelDia(desdeFinal), finDelDia(hastaFinal));
    }

    @GetMapping("/stock-bajo")
    public List<Producto> stockBajo() {
        return productoRepository.buscarStockBajo();
    }

    // Datos "seguros" (sin costos ni ganancia) para la pantalla de Inicio, visible a cualquier rol
    @GetMapping("/resumen-hoy")
    public ResumenHoyResponse resumenHoy() {
        LocalDate hoy = LocalDate.now();
        ResumenVentasResponse ventas = ventaRepository.resumenVentas(inicioDelDia(hoy), finDelDia(hoy));
        int stockBajo = productoRepository.buscarStockBajo().size();
        return new ResumenHoyResponse(ventas.cantidadVentas(), ventas.totalVendido(), stockBajo);
    }

    @GetMapping("/ventas-por-usuario")
    public List<VentaPorUsuarioResponse> ventasPorUsuario(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate hastaFinal = hasta != null ? hasta : LocalDate.now();
        LocalDate desdeFinal = desde != null ? desde : hastaFinal.withDayOfMonth(1);
        return ventaRepository.ventasPorUsuario(inicioDelDia(desdeFinal), finDelDia(hastaFinal));
    }

    // Totales por método de pago en un rango de fechas (a diferencia de totalesPorMetodoPagoDeCaja,
    // que es de una sola caja, para la reconciliación de efectivo)
    @GetMapping("/metodos-pago")
    public List<TotalPorMetodoPagoResponse> metodosPago(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate hastaFinal = hasta != null ? hasta : LocalDate.now();
        LocalDate desdeFinal = desde != null ? desde : hastaFinal.withDayOfMonth(1);
        return ventaRepository.totalesPorMetodoPagoEnPeriodo(inicioDelDia(desdeFinal), finDelDia(hastaFinal));
    }

    // Reporte de ventas filtrable (a diferencia de /api/ventas, que es el registro operativo de tickets)
    @GetMapping("/ventas")
    public List<VentaReporteResponse> ventas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) Long cajaId) {
        LocalDate hastaFinal = hasta != null ? hasta : LocalDate.now();
        LocalDate desdeFinal = desde != null ? desde : hastaFinal.withDayOfMonth(1);
        return ventaRepository.buscarFiltradas(inicioDelDia(desdeFinal), finDelDia(hastaFinal), usuarioId, metodoPago, cajaId);
    }

    @GetMapping("/inventario-movimientos")
    public List<MovimientoInventarioReporteResponse> inventarioMovimientos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long productoId) {
        LocalDate hastaFinal = hasta != null ? hasta : LocalDate.now();
        LocalDate desdeFinal = desde != null ? desde : hastaFinal.withDayOfMonth(1);
        return movimientoInventarioRepository.buscarFiltrados(inicioDelDia(desdeFinal), finDelDia(hastaFinal), productoId);
    }

    private OffsetDateTime inicioDelDia(LocalDate fecha) {
        return fecha.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private OffsetDateTime finDelDia(LocalDate fecha) {
        return fecha.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime().minusNanos(1);
    }
}
