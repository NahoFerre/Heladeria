package com.heladeria.backend.compra;

import com.heladeria.backend.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
@Getter
@Setter
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String proveedor;

    @Size(max = 50)
    private String numeroFactura;

    private String metodoPago;

    @Size(max = 300)
    private String observaciones;

    private BigDecimal total;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Se congela el username al momento del registro para que el historial sobreviva si el usuario es eliminado
    private String usuarioNombreMomento;

    @Column(updatable = false)
    private OffsetDateTime fecha;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraItem> items = new ArrayList<>();

    @PrePersist
    protected void alCrear() {
        fecha = OffsetDateTime.now();
    }
}
