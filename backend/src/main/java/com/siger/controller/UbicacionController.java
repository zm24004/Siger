package com.siger.controller;

import com.siger.dto.UbicacionRequest;
import com.siger.dto.UbicacionResponse;
import com.siger.service.UbicacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ubicaciones (catalogue read is public, creation requires ORGANIZADOR).
 */
@RestController
@RequestMapping("/api/ubicaciones")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    public UbicacionController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @GetMapping
    public ResponseEntity<List<UbicacionResponse>> listar() {
        return ResponseEntity.ok(ubicacionService.listar());
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<UbicacionResponse> crear(@Valid @RequestBody UbicacionRequest req) {
        return ResponseEntity.ok(ubicacionService.crear(req));
    }
}
