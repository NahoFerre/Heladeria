package com.heladeria.backend.promocion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    List<Promocion> findByActivoTrueOrderByNombre();

    List<Promocion> findAllByOrderByNombre();
}
