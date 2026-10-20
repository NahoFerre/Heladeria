package com.heladeria.backend.producto;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ProductoVariante v where v.id = :id")
    Optional<ProductoVariante> buscarParaActualizarStock(@Param("id") Long id);
}
