package com.siger.controller;

import com.siger.dto.PageResponse;
import com.siger.dto.RecursoRequest;
import com.siger.dto.RecursoResponse;
import com.siger.security.Permisos;
import com.siger.service.RecursoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * CRUD de recursos para administradores y organizadores autorizados.
 */
@RestController
@RequestMapping("/api/v1/recursos")
@Validated
public class RecursoController {

    private final RecursoService recursoService;

    public RecursoController(RecursoService recursoService) {
        this.recursoService = recursoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permisos.RESOURCES_READ + "')")
    public ResponseEntity<PageResponse<RecursoResponse>> listar(
        @RequestParam(defaultValue = "1") @Min(1) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return ResponseEntity.ok(recursoService.listar(page, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.RESOURCES_READ + "')")
    public ResponseEntity<RecursoResponse> detalle(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(recursoService.detalle(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permisos.RESOURCES_CREATE + "')")
    public ResponseEntity<RecursoResponse> crear(@Valid @RequestBody RecursoRequest req) {
        return ResponseEntity.status(201).body(recursoService.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.RESOURCES_UPDATE + "')")
    public ResponseEntity<RecursoResponse> actualizar(
        @PathVariable @Positive Long id,
        @Valid @RequestBody RecursoRequest req
    ) {
        return ResponseEntity.ok(recursoService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.RESOURCES_DELETE + "')")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        recursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
