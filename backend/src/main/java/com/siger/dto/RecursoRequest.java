package com.siger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecursoRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    String descripcion,

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que 0")
    Integer cantidad,

    String tipo,

    String estado
) {
}
