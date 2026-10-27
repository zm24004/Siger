package com.siger.dto;

import java.util.List;

public record RolResponse(String idRol, String nombre, List<PermisoResponse> permisos) {
}
