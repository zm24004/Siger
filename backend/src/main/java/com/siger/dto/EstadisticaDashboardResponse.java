package com.siger.dto;

public record EstadisticaDashboardResponse(
    long totalEventos,
    long totalReservaciones,
    long totalAsistentes,
    double porcentajeAsistencia
) {
}
