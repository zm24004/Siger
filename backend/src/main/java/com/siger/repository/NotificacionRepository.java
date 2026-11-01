package com.siger.repository;

import com.siger.model.Notificacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuario_IdUsuarioOrderByLeidoAscFechaEnvioDesc(Long idUsuario);

    @Query("""
        select n from Notificacion n
        where n.usuario.idUsuario = :idUsuario
          and (
            :cursorFecha is null
            or n.fechaEnvio < :cursorFecha
            or (n.fechaEnvio = :cursorFecha and n.idNotificacion < :cursorId)
          )
        order by n.fechaEnvio desc, n.idNotificacion desc
        """)
    List<Notificacion> buscarPagina(
        @Param("idUsuario") Long idUsuario,
        @Param("cursorFecha") LocalDateTime cursorFecha,
        @Param("cursorId") Long cursorId,
        Pageable pageable);

    long countByUsuario_IdUsuarioAndLeidoFalse(Long idUsuario);
    // long countByidUsuarioAndLeidoFalse(Long idUsuario);

    java.util.Optional<Notificacion> findByIdNotificacionAndUsuario_IdUsuario(
        Long idNotificacion, Long idUsuario);
}
