package com.siger.controller;

import com.siger.dto.RecursoRequest;
import com.siger.dto.RecursoResponse;
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
@PreAuthorize("hasRole('ORGANIZADOR')")
public class RecursoController {

    private final RecursoService recursoService;

    public RecursoController(RecursoService recursoService) {
        this.recursoService = recursoService;
    }

    @GetMapping
    public ResponseEntity<List<RecursoResponse>> listar() {
        return ResponseEntity.ok(recursoService.listar());
    }

    @PostMapping
    public ResponseEntity<RecursoResponse> crear(@Valid @RequestBody RecursoRequest req) {
        return ResponseEntity.ok(recursoService.crear(req));
    }
}
