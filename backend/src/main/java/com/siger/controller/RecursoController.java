package com.siger.controller;

import com.siger.dto.RecursoRequest;
import com.siger.dto.RecursoResponse;
import com.siger.security.Permisos;
import com.siger.service.RecursoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Recurso CRUD (organizador-only).
 */
@RestController
@RequestMapping("/api/recursos")
public class RecursoController {

    private final RecursoService recursoService;

    public RecursoController(RecursoService recursoService) {
        this.recursoService = recursoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permisos.RESOURCES_READ + "')")
    public ResponseEntity<List<RecursoResponse>> listar() {
        return ResponseEntity.ok(recursoService.listar());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permisos.RESOURCES_CREATE + "')")
    public ResponseEntity<RecursoResponse> crear(@Valid @RequestBody RecursoRequest req) {
        return ResponseEntity.ok(recursoService.crear(req));
    }
}
