package com.heladeria.backend.auditoria;

import com.heladeria.backend.usuario.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AuditoriaService {

    private final RegistroAuditoriaRepository registroAuditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(RegistroAuditoriaRepository registroAuditoriaRepository, UsuarioRepository usuarioRepository) {
        this.registroAuditoriaRepository = registroAuditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrar(String username, TipoAccionAuditoria accion, String entidad, Long entidadId, String detalle) {
        RegistroAuditoria registro = new RegistroAuditoria();
        if (username != null) {
            usuarioRepository.findByUsername(username).ifPresent(registro::setUsuario);
        }
        registro.setUsuarioNombreMomento(username);
        registro.setAccion(accion);
        registro.setEntidad(entidad);
        registro.setEntidadId(entidadId);
        registro.setDetalle(detalle);
        registroAuditoriaRepository.save(registro);
    }

    public Page<RegistroAuditoria> listar(OffsetDateTime desde, OffsetDateTime hasta, Long usuarioId, Pageable pageable) {
        return registroAuditoriaRepository.buscarFiltrado(desde, hasta, usuarioId, pageable);
    }
}
