package com.siger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ReporteRequest(
    @NotBlank String tipo,
    LocalDate fechaDesde,
    LocalDate fechaHasta,
    @Positive Long idEvento
) {
}
