package com.heladeria.backend.merma;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface MermaRepository extends JpaRepository<Merma, Long> {
    List<Merma> findAllByOrderByFechaDesc();

    @Query("select coalesce(sum(m.costoTotal), 0) from Merma m where m.fecha between :desde and :hasta")
    BigDecimal sumarCostoEnPeriodo(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    // Filtros todos opcionales: sin ninguno, equivale a findAllByOrderByFechaDesc()
    @Query("""
            select m from Merma m
            where (cast(:desde as timestamp) is null or m.fecha >= :desde)
            and (cast(:hasta as timestamp) is null or m.fecha <= :hasta)
            and (:usuarioId is null or m.usuario.id = :usuarioId)
            and (:productoId is null or m.producto.id = :productoId)
            order by m.fecha desc
            """)
    List<Merma> buscarFiltradas(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta,
                                 @Param("usuarioId") Long usuarioId, @Param("productoId") Long productoId);
}
