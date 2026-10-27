package com.siger.dto;

public record EventoDisponibilidadResponse(
    Long idEvento,
    Integer capacidadMaxima,
    long cuposOcupados,
    long cuposDisponibles
) {
}
