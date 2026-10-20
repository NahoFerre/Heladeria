package com.heladeria.backend.producto;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Getter
@Setter
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;

    private String categoria;

    @NotNull
    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal costoUnitario;

    @NotNull
    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal precioVenta;

    private String unidadMedida = "unidad";

    private BigDecimal stockActual = BigDecimal.ZERO;

    private BigDecimal stockMinimo = BigDecimal.ZERO;

    private boolean activo = true;

    @Column(updatable = false)
    private OffsetDateTime creadoEn;

    private OffsetDateTime actualizadoEn;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc")
    private List<ProductoVariante> variantes = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc")
    private List<GrupoAdicionales> gruposAdicionales = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc")
    private List<RecetaItem> receta = new ArrayList<>();

    @PrePersist
    protected void alCrear() {
        creadoEn = OffsetDateTime.now();
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    protected void alActualizar() {
        actualizadoEn = OffsetDateTime.now();
    }
}
