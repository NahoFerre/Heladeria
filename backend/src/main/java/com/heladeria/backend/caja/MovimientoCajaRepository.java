package com.heladeria.backend.caja;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    List<MovimientoCaja> findByCajaIdOrderByFechaDesc(Long cajaId);

    @Query("select coalesce(sum(m.monto), 0) from MovimientoCaja m where m.caja.id = :cajaId and m.tipo = :tipo")
    BigDecimal sumarPorTipo(@Param("cajaId") Long cajaId, @Param("tipo") TipoMovimientoCaja tipo);
}
