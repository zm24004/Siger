package com.siger.controller;

import com.siger.dto.UsuarioEstadoRequest;
import com.siger.dto.UsuarioCreateRequest;
import com.siger.dto.UsuarioPageResponse;
import com.siger.dto.UsuarioResponse;
import com.siger.dto.UsuarioUpdateRequest;
import com.siger.security.Permisos;
import com.siger.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only user management endpoints.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;


    @GetMapping
    @PreAuthorize("hasAuthority('" + Permisos.USERS_READ + "')")
    public ResponseEntity<UsuarioPageResponse> listar(
        @RequestParam(required = false) String rol,
        @RequestParam(required = false) Boolean estado,
        @RequestParam(defaultValue = "1") @jakarta.validation.constraints.Min(1) int page,
        @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1)
            @jakarta.validation.constraints.Max(100) int limit
    ) {
        return ResponseEntity.ok(usuarioService.listar(rol, estado, page, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.USERS_READ + "')")
    public ResponseEntity<UsuarioResponse> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.detalle(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permisos.USERS_CREATE + "')")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        return ResponseEntity.status(201).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.USERS_UPDATE + "')")
    public ResponseEntity<UsuarioResponse> actualizar(
        @PathVariable Long id,
        @Valid @RequestBody UsuarioUpdateRequest request
    ) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('" + Permisos.USERS_STATE + "')")
    public ResponseEntity<UsuarioResponse> cambiarEstado(@PathVariable Long id,
                                                         @Valid @RequestBody UsuarioEstadoRequest req) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.USERS_DELETE + "')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
