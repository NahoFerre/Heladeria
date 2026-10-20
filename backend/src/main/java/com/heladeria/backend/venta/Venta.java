package com.heladeria.backend.venta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heladeria.backend.caja.Caja;
import com.heladeria.backend.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter
@Setter
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    private OffsetDateTime fecha;

    private BigDecimal total;

    private String metodoPago = "EFECTIVO";

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "caja_id")
    @JsonIgnore
    private Caja caja;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaItem> items = new ArrayList<>();

    // null = todavía no se imprimió; se actualiza cada vez que se imprime (también sirve de "última vez impreso")
    private OffsetDateTime impresoEn;

    @Enumerated(EnumType.STRING)
    private EstadoVenta estado = EstadoVenta.CONFIRMADA;

    private OffsetDateTime anuladaEn;

    @ManyToOne
    @JoinColumn(name = "anulada_por_id")
    private Usuario anuladaPor;

    private String motivoAnulacion;

    @PrePersist
    protected void alCrear() {
        fecha = OffsetDateTime.now();
    }
}
