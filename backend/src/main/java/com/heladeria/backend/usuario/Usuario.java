package com.heladeria.backend.usuario;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @JsonIgnore
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private RolUsuario rol;

    @Column(updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void alCrear() {
        creadoEn = OffsetDateTime.now();
    }
}
