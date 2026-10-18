package com.siger.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservacionResponse(
    Long idReservacion,
    String codigoReservacion,
    String estado,
    LocalDate fechaReservacion,
    Long idEvento,
    String eventoNombre,
    LocalDate eventoFecha,
    LocalTime eventoHoraInicio,
    String participanteNombre
) {
}
