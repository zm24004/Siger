package com.siger.dto;

import jakarta.validation.constraints.NotBlank;

public record AsistenciaRequest(
    @NotBlank(message = "El código de reservación es obligatorio")
    String codigoReservacion
) {
}
