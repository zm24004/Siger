package com.siger.repository;

import com.siger.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuario_IdUsuarioOrderByFechaEnvioDesc(Long idUsuario);

    long countByUsuario_IdUsuarioAndLeidoFalse(Long idUsuario);
}
