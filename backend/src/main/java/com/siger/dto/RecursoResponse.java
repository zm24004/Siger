package com.siger.dto;

public record RecursoResponse(
    Long idRecurso,
    String nombre,
    String descripcion,
    Integer cantidad,
    String tipo,
    String estado
) {
}
