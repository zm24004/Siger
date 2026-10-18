package com.siger.dto;

import jakarta.validation.constraints.NotNull;

public record UsuarioEstadoRequest(
    @NotNull(message = "El estado es obligatorio")
    Boolean estado
) {
}
