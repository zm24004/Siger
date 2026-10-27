package com.siger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UbicacionRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    String nombre,

    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    String direccion,

    @Size(max = 50, message = "El tipo no puede exceder 50 caracteres")
    String tipo,

    @Size(max = 500, message = "El enlace virtual no puede exceder 500 caracteres")
    String enlaceVirtual
) {
}
