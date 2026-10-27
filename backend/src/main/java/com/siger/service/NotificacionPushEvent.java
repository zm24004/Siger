package com.siger.service;

import com.siger.dto.NotificacionResponse;

public record NotificacionPushEvent(
    String emailUsuario,
    NotificacionResponse notificacion
) {
}
