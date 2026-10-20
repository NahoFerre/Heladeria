package com.heladeria.backend.insumo;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "insumos")
@Getter
@Setter
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;

    @Enumerated(EnumType.STRING)
    private UnidadMedida unidadMedida = UnidadMedida.UNIDAD;

    @NotNull
    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal costoUnitario;

    private BigDecimal stockActual = BigDecimal.ZERO;

    private BigDecimal stockMinimo = BigDecimal.ZERO;

    private String proveedor;

    private boolean activo = true;

    @Column(updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void alCrear() {
        creadoEn = OffsetDateTime.now();
    }
}
