package com.siger.dto;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int page,
    int limit,
    long totalElements,
    int totalPages
) {
}
