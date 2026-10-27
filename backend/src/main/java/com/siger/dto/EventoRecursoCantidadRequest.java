package com.siger.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EventoRecursoCantidadRequest(
    @NotNull(message = "La cantidad asignada es obligatoria")
    @Positive(message = "La cantidad asignada debe ser mayor que cero")
    Integer cantidadAsignada
) {
}
