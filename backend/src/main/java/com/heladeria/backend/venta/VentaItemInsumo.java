package com.heladeria.backend.venta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "venta_item_insumos")
@Getter
@Setter
public class VentaItemInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venta_item_id")
    @JsonIgnore
    private VentaItem ventaItem;

    private Long insumoId;

    private String nombreMomento;

    // Cantidad ya multiplicada por lo vendido (no la tasa de receta), para que anular no dependa
    // de recalcular contra una receta que pudo cambiar desde la venta
    private BigDecimal cantidadConsumida;
}
