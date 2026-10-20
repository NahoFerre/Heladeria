package com.heladeria.backend.producto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grupos_adicionales")
@Getter
@Setter
public class GrupoAdicionales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    @JsonIgnore
    private Producto producto;

    private String nombre;
    private int minSeleccion = 0;
    private int maxSeleccion = 1;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc")
    private List<Adicional> opciones = new ArrayList<>();
}
