package com.heladeria.backend.caja;

import com.heladeria.backend.auditoria.AuditoriaService;
import com.heladeria.backend.auditoria.TipoAccionAuditoria;
import com.heladeria.backend.common.CajaException;
import com.heladeria.backend.reporte.TotalPorMetodoPagoResponse;
import com.heladeria.backend.usuario.Usuario;
import com.heladeria.backend.usuario.UsuarioRepository;
import com.heladeria.backend.venta.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CajaService {

    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final AuditoriaService auditoriaService;

    public CajaService(CajaRepository cajaRepository, UsuarioRepository usuarioRepository, VentaRepository ventaRepository,
                        MovimientoCajaRepository movimientoCajaRepository, AuditoriaService auditoriaService) {
        this.cajaRepository = cajaRepository;
        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public CajaActualResponse abrir(AbrirCajaRequest request, String username) {
        if (cajaRepository.findByEstado(EstadoCaja.ABIERTA).isPresent()) {
            throw new CajaException("Ya hay una caja abierta");
        }

        Caja caja = new Caja();
        caja.setMontoApertura(request.montoInicial());
        usuarioRepository.findByUsername(username).ifPresent(caja::setUsuarioApertura);

        Caja guardada = cajaRepository.save(caja);
        auditoriaService.registrar(username, TipoAccionAuditoria.CAJA_ABIERTA, "CAJA", guardada.getId(),
                "Monto inicial: " + guardada.getMontoApertura());
        return construirActual(guardada);
    }

    @Transactional
    public CierreCajaResponse cerrar(CerrarCajaRequest request, String username) {
        Caja caja = cajaRepository.findByEstado(EstadoCaja.ABIERTA)
                .orElseThrow(() -> new CajaException("No hay una caja abierta para cerrar"));

        List<TotalPorMetodoPagoResponse> totales = ventaRepository.totalesPorMetodoPagoDeCaja(caja.getId());
        BigDecimal efectivoVendido = totalPara(totales, "EFECTIVO");
        BigDecimal totalVentas = totales.stream()
                .map(TotalPorMetodoPagoResponse::totalVendido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ingresos = movimientoCajaRepository.sumarPorTipo(caja.getId(), TipoMovimientoCaja.INGRESO);
        BigDecimal retiros = movimientoCajaRepository.sumarPorTipo(caja.getId(), TipoMovimientoCaja.RETIRO);
        BigDecimal gastos = movimientoCajaRepository.sumarPorTipo(caja.getId(), TipoMovimientoCaja.GASTO);
        BigDecimal esperado = caja.getMontoApertura().add(efectivoVendido).add(ingresos).subtract(retiros).subtract(gastos);

        caja.setMontoCierreEsperado(esperado);
        caja.setMontoCierreContado(request.montoContado());
        caja.setDiferencia(request.montoContado().subtract(esperado));
        caja.setTotalVentas(totalVentas);
        caja.setObservacion(request.observacion());
        usuarioRepository.findByUsername(username).ifPresent(caja::setUsuarioCierre);
        caja.setEstado(EstadoCaja.CERRADA);
        caja.setCerradaEn(OffsetDateTime.now());

        Caja cerrada = cajaRepository.save(caja);

        auditoriaService.registrar(username, TipoAccionAuditoria.CAJA_CERRADA, "CAJA", cerrada.getId(),
                "Esperado: " + esperado + ", contado: " + cerrada.getMontoCierreContado() + ", diferencia: " + cerrada.getDiferencia());

        return new CierreCajaResponse(
                cerrada.getId(), cerrada.getAbiertaEn(), cerrada.getCerradaEn(),
                cerrada.getMontoApertura(), cerrada.getMontoCierreEsperado(), cerrada.getMontoCierreContado(),
                cerrada.getDiferencia(), cerrada.getTotalVentas(),
                nombreDe(cerrada.getUsuarioApertura()), nombreDe(cerrada.getUsuarioCierre()),
                cerrada.getObservacion(), totales, ingresos, retiros, gastos
        );
    }

    @Transactional
    public MovimientoCajaResponse registrarMovimiento(MovimientoCajaRequest request, String username) {
        Caja caja = cajaRepository.findByEstado(EstadoCaja.ABIERTA)
                .orElseThrow(() -> new CajaException("No hay una caja abierta"));

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setCaja(caja);
        movimiento.setTipo(request.tipo());
        movimiento.setMonto(request.monto());
        movimiento.setMotivo(request.motivo());
        usuarioRepository.findByUsername(username).ifPresent(movimiento::setUsuario);
        movimiento.setUsuarioNombreMomento(username);

        MovimientoCaja guardado = movimientoCajaRepository.save(movimiento);

        TipoAccionAuditoria accion = switch (request.tipo()) {
            case RETIRO -> TipoAccionAuditoria.RETIRO_EFECTIVO;
            case INGRESO -> TipoAccionAuditoria.INGRESO_EFECTIVO;
            case GASTO -> TipoAccionAuditoria.GASTO_REGISTRADO;
        };
        auditoriaService.registrar(username, accion, "CAJA", caja.getId(), request.motivo() + " (" + request.monto() + ")");

        return MovimientoCajaResponse.desde(guardado);
    }

    public List<MovimientoCajaResponse> listarMovimientosCajaAbierta() {
        return cajaRepository.findByEstado(EstadoCaja.ABIERTA)
                .map(c -> movimientoCajaRepository.findByCajaIdOrderByFechaDesc(c.getId()))
                .orElse(List.of())
                .stream().map(MovimientoCajaResponse::desde).toList();
    }

    public List<MovimientoCajaResponse> listarMovimientos(Long cajaId) {
        return movimientoCajaRepository.findByCajaIdOrderByFechaDesc(cajaId).stream()
                .map(MovimientoCajaResponse::desde).toList();
    }

    public Optional<CajaActualResponse> obtenerActual() {
        return cajaRepository.findByEstado(EstadoCaja.ABIERTA).map(this::construirActual);
    }

    public List<CajaHistorialResponse> historial() {
        return cajaRepository.findByEstadoOrderByAbiertaEnDesc(EstadoCaja.CERRADA).stream()
                .map(c -> new CajaHistorialResponse(
                        c.getId(), c.getAbiertaEn(), c.getCerradaEn(), c.getMontoApertura(),
                        c.getMontoCierreEsperado(), c.getMontoCierreContado(), c.getDiferencia(),
                        c.getTotalVentas(), nombreDe(c.getUsuarioApertura()), nombreDe(c.getUsuarioCierre()),
                        movimientoCajaRepository.sumarPorTipo(c.getId(), TipoMovimientoCaja.INGRESO),
                        movimientoCajaRepository.sumarPorTipo(c.getId(), TipoMovimientoCaja.RETIRO),
                        movimientoCajaRepository.sumarPorTipo(c.getId(), TipoMovimientoCaja.GASTO)
                ))
                .toList();
    }

    private CajaActualResponse construirActual(Caja caja) {
        List<TotalPorMetodoPagoResponse> totales = ventaRepository.totalesPorMetodoPagoDeCaja(caja.getId());
        BigDecimal efectivoVendido = totalPara(totales, "EFECTIVO");
        BigDecimal ingresos = movimientoCajaRepository.sumarPorTipo(caja.getId(), TipoMovimientoCaja.INGRESO);
        BigDecimal retiros = movimientoCajaRepository.sumarPorTipo(caja.getId(), TipoMovimientoCaja.RETIRO);
        BigDecimal gastos = movimientoCajaRepository.sumarPorTipo(caja.getId(), TipoMovimientoCaja.GASTO);
        BigDecimal esperadoParcial = caja.getMontoApertura().add(efectivoVendido).add(ingresos).subtract(retiros).subtract(gastos);
        return new CajaActualResponse(
                caja.getId(), caja.getAbiertaEn(), caja.getMontoApertura(),
                nombreDe(caja.getUsuarioApertura()), efectivoVendido, totales,
                ingresos, retiros, gastos, esperadoParcial
        );
    }

    private BigDecimal totalPara(List<TotalPorMetodoPagoResponse> totales, String metodoPago) {
        return totales.stream()
                .filter(t -> metodoPago.equals(t.metodoPago()))
                .findFirst()
                .map(TotalPorMetodoPagoResponse::totalVendido)
                .orElse(BigDecimal.ZERO);
    }

    private String nombreDe(Usuario usuario) {
        return usuario != null ? usuario.getUsername() : null;
    }
}
