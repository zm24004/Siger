package com.siger.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventoResponse(
    Long idEvento,
    String nombre,
    String descripcion,
    LocalDate fecha,
    LocalTime horaInicio,
    LocalTime horaFin,
    Integer capacidadMaxima,
    String estado,
    Long idUbicacion,
    String ubicacionNombre,
    Long idOrganizador,
    String organizadorNombre,
    long cuposDisponibles
) {
}
