package com.siger.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReprogramarEventoRequest(
    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha del evento no puede estar en el pasado")
    LocalDate fecha,

    @NotNull(message = "La hora de inicio es obligatoria")
    LocalTime horaInicio,

    LocalTime horaFin
) {
}
