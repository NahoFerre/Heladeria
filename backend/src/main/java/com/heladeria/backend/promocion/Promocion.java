package com.heladeria.backend.promocion;

import com.heladeria.backend.producto.Producto;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "promociones")
@Getter
@Setter
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoPromocion tipo;

    // Según el tipo: porcentaje (0-100), monto a descontar, o el precio final fijo
    @NotNull
    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal valor;

    // Condiciones, todas opcionales. Sin producto ni categoría, aplica a todo el catálogo.
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private String categoria;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal cantidadMinima = BigDecimal.ONE;

    private boolean activo = true;

    @Column(updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void alCrear() {
        creadoEn = OffsetDateTime.now();
    }
}
