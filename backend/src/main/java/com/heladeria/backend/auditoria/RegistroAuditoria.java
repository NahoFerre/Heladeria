package com.heladeria.backend.auditoria;

import com.heladeria.backend.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "registros_auditoria")
@Getter
@Setter
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Se congela el username al momento del registro para que el historial sobreviva si el usuario es eliminado
    private String usuarioNombreMomento;

    @Enumerated(EnumType.STRING)
    private TipoAccionAuditoria accion;

    private String entidad;
    private Long entidadId;
    private String detalle;
    private String ip;

    @Column(updatable = false)
    private OffsetDateTime fecha;

    @PrePersist
    protected void alCrear() {
        fecha = OffsetDateTime.now();
    }
}
