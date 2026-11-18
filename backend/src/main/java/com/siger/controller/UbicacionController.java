package com.siger.controller;

import com.siger.dto.PageResponse;
import com.siger.dto.UbicacionRequest;
import com.siger.dto.UbicacionResponse;
import com.siger.security.Permisos;
import com.siger.service.UbicacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * CRUD de ubicaciones para administradores y organizadores autorizados.
 */
@RestController
@RequestMapping("/api/v1/ubicaciones")
@Validated
public class UbicacionController {

    private final UbicacionService ubicacionService;

    public UbicacionController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permisos.LOCATIONS_READ + "')")
    public ResponseEntity<PageResponse<UbicacionResponse>> listar(
        @RequestParam(required = false) String tipo,
        @RequestParam(defaultValue = "1") @Min(1) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return ResponseEntity.ok(ubicacionService.listar(tipo, page, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.LOCATIONS_READ + "')")
    public ResponseEntity<UbicacionResponse> detalle(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ubicacionService.detalle(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permisos.LOCATIONS_CREATE + "')")
    public ResponseEntity<UbicacionResponse> crear(@Valid @RequestBody UbicacionRequest req) {
        return ResponseEntity.status(201).body(ubicacionService.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.LOCATIONS_UPDATE + "')")
    public ResponseEntity<UbicacionResponse> actualizar(
        @PathVariable @Positive Long id,
        @Valid @RequestBody UbicacionRequest req
    ) {
        return ResponseEntity.ok(ubicacionService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.LOCATIONS_DELETE + "')")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        ubicacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
