package com.siger.dto;

import java.util.List;

public record UsuarioPageResponse(
    List<UsuarioResponse> content,
    int page,
    int limit,
    long totalElements,
    int totalPages
) {
}
