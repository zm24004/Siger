package com.siger.dto;

import java.time.LocalDate;

public record EstadisticaEventoResponse(
    Long idEvento,
    String nombre,
    LocalDate fecha,
    Integer capacidadMaxima,
    Long reservacionesActivas,
    Long asistentes
) {
}
