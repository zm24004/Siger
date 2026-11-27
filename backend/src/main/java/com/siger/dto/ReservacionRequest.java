package com.siger.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservacionRequest(
    @NotNull(message = "El identificador del evento es obligatorio")
    @Positive(message = "El identificador del evento debe ser positivo")
    Long idEvento
) {
}
