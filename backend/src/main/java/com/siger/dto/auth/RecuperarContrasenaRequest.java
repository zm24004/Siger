package com.siger.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud de recuperación de contraseña.
 * El backend SIEMPRE responde el mismo mensaje, sin revelar si el email existe.
 */
public record RecuperarContrasenaRequest(
    @NotBlank(message = "El email es obligatorio")
    String email,

    @NotBlank(message = "El nombre o alias es obligatorio")
    String nombreOAlias
) {
}
