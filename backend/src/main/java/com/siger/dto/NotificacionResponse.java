package com.siger.dto;

import java.time.LocalDateTime;

public record NotificacionResponse(
    Long idNotificacion,
    String mensaje,
    String tipo,
    Boolean leido,
    LocalDateTime fechaEnvio
) {
}
