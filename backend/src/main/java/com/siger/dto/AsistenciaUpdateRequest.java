package com.siger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AsistenciaUpdateRequest(
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "(?i)Presente|Ausente", message = "El estado debe ser Presente o Ausente")
    String estado
) {
}
