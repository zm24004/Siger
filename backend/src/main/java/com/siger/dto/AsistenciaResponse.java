package com.siger.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AsistenciaResponse(
    Long idAsistencia,
    String estado,
    LocalDate fechaAsistencia,
    LocalTime horaAsistencia,
    Long idEvento,
    String eventoNombre,
    String codigoReservacion,
    String participanteNombre
) {
}
