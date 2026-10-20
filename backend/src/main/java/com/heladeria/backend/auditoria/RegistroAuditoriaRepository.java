package com.heladeria.backend.auditoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {

    // Filtros todos opcionales
    @Query("""
            select r from RegistroAuditoria r
            where (cast(:desde as timestamp) is null or r.fecha >= :desde)
            and (cast(:hasta as timestamp) is null or r.fecha <= :hasta)
            and (:usuarioId is null or r.usuario.id = :usuarioId)
            order by r.fecha desc
            """)
    Page<RegistroAuditoria> buscarFiltrado(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta,
                                            @Param("usuarioId") Long usuarioId, Pageable pageable);
}
