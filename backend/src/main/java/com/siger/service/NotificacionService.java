package com.siger.service;

import com.siger.dto.NotificacionResponse;
import com.siger.exception.NotFoundException;
import com.siger.model.Notificacion;
import com.siger.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User-scoped notification operations (list, mark-as-read, unread count).
 */
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorUsuario(Long idUsuario) {
        return notificacionRepository
            .findByUsuario_IdUsuarioOrderByFechaEnvioDesc(idUsuario)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(Long idUsuario) {
        return notificacionRepository.countByUsuario_IdUsuarioAndLeidoFalse(idUsuario);
    }

    @Transactional
    public NotificacionResponse marcarLeida(Long idNotificacion) {
        Notificacion n = notificacionRepository.findById(idNotificacion)
            .orElseThrow(() -> new NotFoundException("Notificación no encontrada: " + idNotificacion));
        n.setLeido(true);
        Notificacion saved = notificacionRepository.save(n);
        return toResponse(saved);
    }

    private NotificacionResponse toResponse(Notificacion n) {
        return new NotificacionResponse(
            n.getIdNotificacion(),
            n.getMensaje(),
            n.getTipo(),
            n.getLeido(),
            n.getFechaEnvio()
        );
    }
}
