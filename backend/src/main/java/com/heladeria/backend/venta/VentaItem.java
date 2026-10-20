package com.heladeria.backend.venta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heladeria.backend.producto.Producto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta_items")
@Getter
@Setter
public class VentaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venta_id")
    @JsonIgnore
    private Venta venta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private BigDecimal cantidad;

    // Precio y costo "congelados" al momento de la venta: si luego cambian en el producto,
    // los reportes históricos de ganancia no se distorsionan
    private BigDecimal precioUnitarioMomento;
    private BigDecimal costoUnitarioMomento;

    // Variante elegida (si el producto tiene): precio/costo de arriba ya reflejan la variante, no el producto base
    private Long varianteId;
    private String varianteNombreMomento;

    // Promoción aplicada automáticamente (si hubo alguna): precioUnitarioMomento de arriba ya viene con el
    // descuento aplicado. Se congela el nombre para que sobreviva si la promoción se edita o desactiva después.
    private Long promocionId;
    private String promocionNombreMomento;

    @OneToMany(mappedBy = "ventaItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaItemAdicional> adicionales = new ArrayList<>();

    @OneToMany(mappedBy = "ventaItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaItemInsumo> insumosConsumidos = new ArrayList<>();
}
