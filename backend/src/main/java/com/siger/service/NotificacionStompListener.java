package com.siger.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificacionStompListener {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionStompListener.class);

    private final SimpMessagingTemplate simpMessagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void enviar(NotificacionPushEvent event) {
        try {
            simpMessagingTemplate.convertAndSendToUser(
                event.emailUsuario(),
                "/queue/notificaciones",
                event.notificacion());
        } catch (RuntimeException ex) {
            LOG.warn("No se pudo enviar la notificación {} por WebSocket al usuario {}",
                event.notificacion().idNotificacion(), event.emailUsuario(), ex);
        }
    }
}
