package com.heladeria.backend.venta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "venta_item_adicionales")
@Getter
@Setter
public class VentaItemAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venta_item_id")
    @JsonIgnore
    private VentaItem ventaItem;

    private Long adicionalId;

    // Nombre, precio y costo "congelados" al momento de la venta, igual que en VentaItem
    private String nombreMomento;
    private BigDecimal precioAdicionalMomento;
    private BigDecimal costoAdicionalMomento;

    // null = este adicional no controlaba stock al momento de vender; si no es null, es la cantidad
    // que se descontó (para poder revertirla exactamente si la venta se anula)
    private BigDecimal cantidadStockDescontada;
}
