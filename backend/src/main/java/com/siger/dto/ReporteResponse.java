package com.siger.dto;

import java.time.LocalDateTime;

public record ReporteResponse(
    Long idReporte,
    String tipo,
    String parametro,
    String contenido,
    LocalDateTime fechaGeneracion,
    Long idCreador,
    String nombreCreador
) {
}
