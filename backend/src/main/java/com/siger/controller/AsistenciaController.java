package com.siger.controller;

import com.siger.dto.AsistenciaRequest;
import com.siger.dto.AsistenciaResponse;
import com.siger.service.AsistenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Asistencia registration (organizador-only).
 */
@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @PostMapping("/registrar")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<AsistenciaResponse> registrar(@Valid @RequestBody AsistenciaRequest req) {
        return ResponseEntity.ok(asistenciaService.registrar(req));
    }
}
