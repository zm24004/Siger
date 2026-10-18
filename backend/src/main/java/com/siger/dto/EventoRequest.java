package com.siger.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventoRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    String descripcion,

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha del evento no puede estar en el pasado")
    LocalDate fecha,

    @NotNull(message = "La hora de inicio es obligatoria")
    LocalTime horaInicio,

    LocalTime horaFin,

    @NotNull(message = "La capacidad máxima es obligatoria")
    @Positive(message = "La capacidad máxima debe ser mayor que 0")
    Integer capacidadMaxima,

    @NotNull(message = "La ubicación es obligatoria")
    Long idUbicacion
) {
}
