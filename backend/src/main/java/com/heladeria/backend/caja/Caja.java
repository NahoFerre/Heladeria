package com.heladeria.backend.caja;

import com.heladeria.backend.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cajas")
@Getter
@Setter
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EstadoCaja estado = EstadoCaja.ABIERTA;

    private BigDecimal montoApertura;
    private BigDecimal montoCierreEsperado;
    private BigDecimal montoCierreContado;
    private BigDecimal diferencia;

    // Suma de todos los métodos de pago del turno (informativo, no interviene en la reconciliación de efectivo)
    private BigDecimal totalVentas;

    private String observacion;

    @ManyToOne
    @JoinColumn(name = "usuario_apertura_id")
    private Usuario usuarioApertura;

    @ManyToOne
    @JoinColumn(name = "usuario_cierre_id")
    private Usuario usuarioCierre;

    @Column(updatable = false)
    private OffsetDateTime abiertaEn;

    private OffsetDateTime cerradaEn;

    @PrePersist
    protected void alCrear() {
        abiertaEn = OffsetDateTime.now();
    }
}
