package com.siger.dto;

import java.util.List;

public record NotificacionPageResponse(
    List<NotificacionResponse> content,
    NotificacionCursor nextCursor,
    boolean hasMore
) {
}
