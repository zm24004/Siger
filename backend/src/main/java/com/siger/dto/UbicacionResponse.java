package com.siger.dto;

public record UbicacionResponse(
    Long idUbicacion,
    String nombre,
    String direccion,
    String tipo,
    String enlaceVirtual
) {
}
