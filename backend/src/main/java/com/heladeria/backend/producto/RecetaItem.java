package com.heladeria.backend.producto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heladeria.backend.insumo.Insumo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "receta_items")
@Getter
@Setter
public class RecetaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exactamente uno de producto/variante debe estar seteado (lo garantiza el service, no la base)
    @ManyToOne
    @JoinColumn(name = "producto_id")
    @JsonIgnore
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "variante_id")
    @JsonIgnore
    private ProductoVariante variante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    private BigDecimal cantidad;
}
