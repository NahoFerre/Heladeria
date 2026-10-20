package com.heladeria.backend.gasto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {
    List<Gasto> findAllByOrderByFechaDesc();

    @Query("select coalesce(sum(g.monto), 0) from Gasto g where g.fecha between :desde and :hasta")
    BigDecimal sumarMontoEnPeriodo(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    // Filtros todos opcionales: sin ninguno, equivale a findAllByOrderByFechaDesc()
    @Query("""
            select g from Gasto g
            where (cast(:desde as timestamp) is null or g.fecha >= :desde)
            and (cast(:hasta as timestamp) is null or g.fecha <= :hasta)
            and (:usuarioId is null or g.usuario.id = :usuarioId)
            and (:metodoPago is null or g.metodoPago = :metodoPago)
            and (:categoria is null or g.categoria = :categoria)
            order by g.fecha desc
            """)
    List<Gasto> buscarFiltrados(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta,
                                 @Param("usuarioId") Long usuarioId, @Param("metodoPago") String metodoPago,
                                 @Param("categoria") String categoria);
}
