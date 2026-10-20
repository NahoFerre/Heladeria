package com.heladeria.backend.producto;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdicionalRepository extends JpaRepository<Adicional, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Adicional a where a.id = :id")
    Optional<Adicional> buscarParaActualizarStock(@Param("id") Long id);
}
