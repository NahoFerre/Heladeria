package com.heladeria.backend.inventario;

import com.heladeria.backend.producto.Producto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Getter
@Setter
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;

    private BigDecimal cantidad;

    private String motivo;

    @Column(updatable = false)
    private OffsetDateTime fecha;

    @PrePersist
    protected void alCrear() {
        fecha = OffsetDateTime.now();
    }
}
