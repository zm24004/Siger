package com.siger.service;

import com.siger.dto.NotificacionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificacionStompListenerTest {

    @Test
    void enviar_enrutaAlUsuarioYDestinoPrivado() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        NotificacionStompListener listener = new NotificacionStompListener(messagingTemplate);
        NotificacionResponse payload = new NotificacionResponse(
            3L, "Evento reprogramado", "Evento", false, LocalDateTime.now());

        listener.enviar(new NotificacionPushEvent("participante@example.test", payload));

        verify(messagingTemplate).convertAndSendToUser(
            "participante@example.test", "/queue/notificaciones", payload);
    }
}
