package com.heladeria.backend.gasto;

import com.heladeria.backend.auditoria.AuditoriaService;
import com.heladeria.backend.auditoria.TipoAccionAuditoria;
import com.heladeria.backend.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class GastoService {

    private final GastoRepository gastoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public GastoService(GastoRepository gastoRepository, UsuarioRepository usuarioRepository, AuditoriaService auditoriaService) {
        this.gastoRepository = gastoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Gasto registrar(GastoRequest request, String username) {
        Gasto gasto = new Gasto();
        gasto.setCategoria(request.categoria());
        gasto.setDescripcion(request.descripcion());
        gasto.setMonto(request.monto());
        gasto.setMetodoPago(request.metodoPago());
        gasto.setObservacion(request.observacion());
        usuarioRepository.findByUsername(username).ifPresent(gasto::setUsuario);
        gasto.setUsuarioNombreMomento(username);

        Gasto guardado = gastoRepository.save(gasto);

        auditoriaService.registrar(username, TipoAccionAuditoria.GASTO_REGISTRADO, "GASTO", guardado.getId(),
                (guardado.getCategoria() != null ? guardado.getCategoria() + ": " : "") + guardado.getDescripcion() + " (" + guardado.getMonto() + ")");

        return guardado;
    }

    public List<Gasto> historial(OffsetDateTime desde, OffsetDateTime hasta, Long usuarioId, String metodoPago, String categoria) {
        return gastoRepository.buscarFiltrados(desde, hasta, usuarioId, metodoPago, categoria);
    }
}
