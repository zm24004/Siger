package com.siger.dto;

import jakarta.validation.constraints.NotBlank;

public record UbicacionRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    String direccion,

    String tipo,

    String enlaceVirtual
) {
}
