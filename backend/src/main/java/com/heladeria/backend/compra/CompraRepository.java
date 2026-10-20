package com.heladeria.backend.compra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findAllByOrderByFechaDesc();

    // Filtros todos opcionales: sin ninguno, equivale a findAllByOrderByFechaDesc()
    @Query("""
            select c from Compra c
            where (cast(:desde as timestamp) is null or c.fecha >= :desde)
            and (cast(:hasta as timestamp) is null or c.fecha <= :hasta)
            and (:usuarioId is null or c.usuario.id = :usuarioId)
            and (:metodoPago is null or c.metodoPago = :metodoPago)
            order by c.fecha desc
            """)
    List<Compra> buscarFiltradas(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta,
                                  @Param("usuarioId") Long usuarioId, @Param("metodoPago") String metodoPago);
}
