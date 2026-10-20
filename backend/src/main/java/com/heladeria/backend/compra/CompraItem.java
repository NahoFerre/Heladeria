package com.heladeria.backend.compra;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heladeria.backend.insumo.Insumo;
import com.heladeria.backend.producto.Producto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "compra_items")
@Getter
@Setter
public class CompraItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "compra_id")
    @JsonIgnore
    private Compra compra;

    // Exactamente uno de insumo/producto debe estar seteado (lo garantiza el service, no la base)
    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private String nombreMomento;

    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal subtotal;
}
