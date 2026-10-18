package com.siger.dto.auth;

/**
 * Respuesta de autenticación: token JWT + datos básicos del usuario.
 */
public record AuthResponse(
    String token,
    String nombre,
    String apellido,
    String rol,
    Long idUsuario
) {
}
