package com.heladeria.backend.producto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "adicionales")
@Getter
@Setter
public class Adicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_id")
    @JsonIgnore
    private GrupoAdicionales grupo;

    private String nombre;
    private BigDecimal precioAdicional = BigDecimal.ZERO;
    private BigDecimal costoAdicional = BigDecimal.ZERO;
    private boolean controlaStock = false;
    private BigDecimal stockActual = BigDecimal.ZERO;
    private BigDecimal stockMinimo = BigDecimal.ZERO;
    private boolean activo = true;
}
