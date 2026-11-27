package com.siger.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EventoRecursoRequest(
    @NotNull(message = "El recurso es obligatorio")
    @Positive(message = "El identificador del recurso debe ser positivo")
    Long idRecurso,

    @NotNull(message = "La cantidad asignada es obligatoria")
    @Positive(message = "La cantidad asignada debe ser mayor que cero")
    Integer cantidadAsignada
) {
}
