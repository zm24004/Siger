package com.siger.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record NotificacionEnvioRequest(
    @NotNull @Positive Long idEvento,
    @Size(max = 1000) String mensaje
) {
}
