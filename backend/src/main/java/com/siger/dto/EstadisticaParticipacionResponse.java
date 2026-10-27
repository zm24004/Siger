package com.siger.dto;

import java.util.List;

public record EstadisticaParticipacionResponse(
    List<EstadisticaEventoResponse> eventos,
    long totalReservaciones,
    long totalAsistentes
) {
}
