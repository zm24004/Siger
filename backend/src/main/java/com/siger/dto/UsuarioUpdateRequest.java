package com.siger.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
    @NotBlank @Size(max = 100) String nombre,
    @NotBlank @Size(max = 100) String apellido,
    @NotBlank @Email @Size(max = 150) String email,
    @NotBlank @Size(max = 30) String telefono,
    @NotBlank @Size(max = 10) String codigoPais
) {
}
