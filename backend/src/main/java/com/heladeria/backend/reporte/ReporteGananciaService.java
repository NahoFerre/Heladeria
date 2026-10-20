package com.heladeria.backend.reporte;

import com.heladeria.backend.gasto.GastoRepository;
import com.heladeria.backend.merma.MermaRepository;
import com.heladeria.backend.venta.VentaItemAdicionalRepository;
import com.heladeria.backend.venta.VentaItemRepository;
import com.heladeria.backend.venta.VentaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Service
public class ReporteGananciaService {

    private final VentaRepository ventaRepository;
    private final VentaItemRepository ventaItemRepository;
    private final VentaItemAdicionalRepository ventaItemAdicionalRepository;
    private final MermaRepository mermaRepository;
    private final GastoRepository gastoRepository;

    public ReporteGananciaService(VentaRepository ventaRepository, VentaItemRepository ventaItemRepository,
                                   VentaItemAdicionalRepository ventaItemAdicionalRepository,
                                   MermaRepository mermaRepository, GastoRepository gastoRepository) {
        this.ventaRepository = ventaRepository;
        this.ventaItemRepository = ventaItemRepository;
        this.ventaItemAdicionalRepository = ventaItemAdicionalRepository;
        this.mermaRepository = mermaRepository;
        this.gastoRepository = gastoRepository;
    }

    public ReporteGananciaResponse calcular(OffsetDateTime desde, OffsetDateTime hasta) {
        ResumenVentasResponse resumen = ventaRepository.resumenVentas(desde, hasta);
        BigDecimal totalVentas = resumen.totalVendido();
        long cantidadVentas = resumen.cantidadVentas();

        BigDecimal costoProductos = ventaItemRepository.costoProductosEnPeriodo(desde, hasta);
        BigDecimal costoAdicionales = ventaItemAdicionalRepository.costoAdicionalesEnPeriodo(desde, hasta);
        BigDecimal totalCosto = costoProductos.add(costoAdicionales);
        long cantidadItemsVendidos = ventaItemRepository.contarItemsEnPeriodo(desde, hasta);

        BigDecimal costoMerma = mermaRepository.sumarCostoEnPeriodo(desde, hasta);
        BigDecimal gastosOperativos = gastoRepository.sumarMontoEnPeriodo(desde, hasta);

        BigDecimal ganancia = totalVentas.subtract(totalCosto);
        BigDecimal gananciaOperativa = ganancia.subtract(costoMerma).subtract(gastosOperativos);

        BigDecimal margenPorcentaje = totalVentas.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : gananciaOperativa.multiply(BigDecimal.valueOf(100)).divide(totalVentas, 2, RoundingMode.HALF_UP);
        BigDecimal ticketPromedio = cantidadVentas == 0
                ? BigDecimal.ZERO
                : totalVentas.divide(BigDecimal.valueOf(cantidadVentas), 2, RoundingMode.HALF_UP);

        return new ReporteGananciaResponse(totalVentas, totalCosto, ganancia, cantidadItemsVendidos, cantidadVentas,
                costoMerma, gastosOperativos, gananciaOperativa, margenPorcentaje, ticketPromedio);
    }
}
