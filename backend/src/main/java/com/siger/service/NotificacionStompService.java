package com.siger.service;

import com.siger.dto.NotificacionResponse;
import com.siger.model.Notificacion;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Publishes a persisted notification for delivery to the owning user's STOMP queue.
 *
 * <p>The user principal name used by {@code SimpMessagingTemplate#convertAndSendToUser}
 * is the email set on the CONNECT frame by
 * {@link com.siger.security.JwtChannelInterceptor}. Spring's user-destination
 * resolver adds the configured {@code /user} prefix automatically, so the
 * client subscribes to {@code /user/queue/notificaciones}.</p>
 *
 * <p>Delivery is deferred until the surrounding transaction commits. A
 * transport failure is logged by {@link NotificacionStompListener}; the
 * persisted notification remains available through the REST list endpoint.</p>
 */
@Service
@RequiredArgsConstructor
public class NotificacionStompService {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Schedules the notification payload for the user identified by their email.
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
        eventPublisher.publishEvent(new NotificacionPushEvent(n.getUsuario().getEmail(), response));
    }
}
