package com.siger.dto;

import java.time.LocalDateTime;

public record NotificacionCursor(
    LocalDateTime fechaEnvio,
    Long idNotificacion
) {
}
