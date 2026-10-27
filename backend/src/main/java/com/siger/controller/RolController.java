package com.siger.controller;

import com.siger.dto.ActualizarPermisosRequest;
import com.siger.dto.RolResponse;
import com.siger.security.Permisos;
import com.siger.service.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permisos.ROLES_READ + "')")
    public ResponseEntity<List<RolResponse>> listar() {
        return ResponseEntity.ok(rolService.listar());
    }

    @PutMapping("/{id}/permisos")
    @PreAuthorize("hasAuthority('" + Permisos.ROLES_UPDATE + "')")
    public ResponseEntity<RolResponse> actualizarPermisos(
        @PathVariable String id,
        @Valid @RequestBody ActualizarPermisosRequest request
    ) {
        return ResponseEntity.ok(rolService.actualizarPermisos(id, request));
    }
}
