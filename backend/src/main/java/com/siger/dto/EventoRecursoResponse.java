package com.siger.dto;

public record EventoRecursoResponse(
    Long idEvento,
    Long idRecurso,
    String nombreRecurso,
    Integer cantidadAsignada
) {
}
