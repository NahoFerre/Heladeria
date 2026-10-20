package com.heladeria.backend.merma;

import com.heladeria.backend.insumo.Insumo;
import com.heladeria.backend.producto.Producto;
import com.heladeria.backend.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mermas")
@Getter
@Setter
public class Merma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private BigDecimal costoTotal;

    @Enumerated(EnumType.STRING)
    private MotivoMerma motivo;

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
