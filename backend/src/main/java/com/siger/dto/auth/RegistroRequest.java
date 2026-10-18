package com.siger.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    String apellido,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    String email,

    @Pattern(regexp = "^$|^[0-9+()\\-\\s]{6,30}$", message = "El teléfono no es válido")
    String telefono,

    @Pattern(regexp = "^$|^\\+?[0-9]{1,4}$", message = "El código de país no es válido (ej: +503)")
    String codigoPais,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String contrasena,

    @NotBlank(message = "El rol es obligatorio")
    String rol
) {
}
