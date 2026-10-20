package com.heladeria.backend.caja;

import com.heladeria.backend.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "movimientos_caja")
@Getter
@Setter
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "caja_id")
    private Caja caja;

    @Enumerated(EnumType.STRING)
    private TipoMovimientoCaja tipo;

    private BigDecimal monto;
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Se congela el username al momento del registro para que el historial sobreviva si el usuario es eliminado
    private String usuarioNombreMomento;

    @Column(updatable = false)
    private OffsetDateTime fecha;

    @PrePersist
    protected void alCrear() {
        fecha = OffsetDateTime.now();
    }
}
