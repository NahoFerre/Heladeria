package com.heladeria.backend.caja;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CajaRepository extends JpaRepository<Caja, Long> {

    Optional<Caja> findByEstado(EstadoCaja estado);

    List<Caja> findByEstadoOrderByAbiertaEnDesc(EstadoCaja estado);
}
