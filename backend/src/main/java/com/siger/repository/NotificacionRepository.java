package com.siger.repository;

import com.siger.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuario_IdUsuarioOrderByLeidoAscFechaEnvioDesc(Long idUsuario);

    long countByUsuario_IdUsuarioAndLeidoFalse(Long idUsuario);
    // long countByidUsuarioAndLeidoFalse(Long idUsuario);

    java.util.Optional<Notificacion> findByIdNotificacionAndUsuario_IdUsuario(
        Long idNotificacion, Long idUsuario);
}
