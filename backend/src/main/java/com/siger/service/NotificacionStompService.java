package com.siger.service;

import com.siger.dto.NotificacionResponse;
import com.siger.model.Notificacion;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Pushes a {@link Notificacion} to the owning user's STOMP queue.
 *
 * <p>The user principal name used by {@link SimpMessagingTemplate#convertAndSendToUser}
 * is the email set on the CONNECT frame by
 * {@link com.siger.security.JwtChannelInterceptor}. Spring's user-destination
 * resolver adds the configured {@code /user} prefix automatically, so the
 * client subscribes to {@code /user/queue/notificaciones}.</p>
 *
 * <p>Failures here must never break the reservation flow: callers (e.g.
 * {@code ReservacionService.reservar}) wrap the call in a try/catch and
 * swallow the exception, since the notification row is already persisted
 * in the database and will be visible via the REST list endpoint.</p>
 */
@Service
@RequiredArgsConstructor
public class NotificacionStompService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Sends the notification payload to the user identified by their email.
     *
     * @param n the persisted notification entity (its {@code usuario} relationship
     *          must provide the email — accessed via {@code n.getUsuario().getEmail()})
     */
    public void enviar(Notificacion n) {
        NotificacionResponse response = new NotificacionResponse(
            n.getIdNotificacion(),
            n.getMensaje(),
            n.getTipo(),
            n.getLeido(),
            n.getFechaEnvio()
        );
        simpMessagingTemplate.convertAndSendToUser(
            n.getUsuario().getEmail(),
            "/queue/notificaciones",
            response
        );
    }
}
