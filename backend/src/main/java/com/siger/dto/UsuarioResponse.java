package com.siger.dto;

import java.time.LocalDateTime;

public record UsuarioResponse(
    Long idUsuario,
    String nombre,
    String apellido,
    String email,
    String telefono,
    String codigoPais,
    Boolean estado,
    String rol,
    LocalDateTime fechaRegistro
) {
}
