package com.siger.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record ActualizarPermisosRequest(@NotNull Set<String> permisos) {
}
