package com.siger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecursoRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    String nombre,

    String descripcion,

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que 0")
    Integer cantidad,

    @Size(max = 50, message = "El tipo no puede exceder 50 caracteres")
    String tipo,

    @Size(max = 20, message = "El estado no puede exceder 20 caracteres")
    String estado
) {
}
