package com.siger.dto;

import java.util.List;

public record EstadisticaReservacionesResponse(
    List<ReservacionesPorEstadoResponse> porEstado,
    long total
) {
}
