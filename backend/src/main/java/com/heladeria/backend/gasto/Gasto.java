package com.heladeria.backend.gasto;

import com.heladeria.backend.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "gastos")
@Getter
@Setter
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoria;

    @NotBlank
    private String descripcion;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal monto;

    private String metodoPago;

    @Size(max = 300)
    private String observacion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Se congela el username al momento del registro para que el historial sobreviva si el usuario es eliminado
    private String usuarioNombreMomento;

    @Column(updatable = false)
    private OffsetDateTime fecha;

    @PrePersist
    protected void alCrear() {
        fecha = OffsetDateTime.now();
    }
}
