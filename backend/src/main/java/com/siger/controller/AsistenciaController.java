package com.siger.controller;

import com.siger.dto.AsistenciaRequest;
import com.siger.dto.AsistenciaResponse;
import com.siger.dto.AsistenciaUpdateRequest;
import com.siger.security.Permisos;
import com.siger.service.AsistenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @PostMapping({"/asistencia", "/asistencias/registrar"})
    @PreAuthorize("hasAuthority('" + Permisos.ATTENDANCE_REGISTER + "')")
    public ResponseEntity<AsistenciaResponse> registrar(
        @Valid @RequestBody AsistenciaRequest request, Authentication authentication) {
        AsistenciaResponse response = asistenciaService.registrar(
            request,
            authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/eventos/{idEvento}/asistencia")
    @PreAuthorize("hasAuthority('" + Permisos.ATTENDANCE_READ + "')")
    public ResponseEntity<List<AsistenciaResponse>> listarPorEvento(
        @PathVariable Long idEvento, Authentication authentication) {
        return ResponseEntity.ok(asistenciaService.listarPorEvento(
            idEvento,
            authentication.getName(),
            SecurityUtils.hasAuthority(Permisos.RESERVATIONS_MANAGE_ALL)));
    }

    @GetMapping("/eventos/{idEvento}/asistencia/lista")
    @PreAuthorize("hasAuthority('" + Permisos.ATTENDANCE_READ + "')")
    public ResponseEntity<List<AsistenciaResponse>> listaPorEvento(
        @PathVariable Long idEvento, Authentication authentication) {
        return ResponseEntity.ok(asistenciaService.listarPorEvento(
            idEvento,
            authentication.getName(),
            false));
    }

    @GetMapping("/asistencia/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.ATTENDANCE_READ + "')")
    public ResponseEntity<AsistenciaResponse> detalle(
        @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(asistenciaService.detalle(
            id,
            authentication.getName(),
            SecurityUtils.hasAuthority(Permisos.RESERVATIONS_MANAGE_ALL)));
    }

    @PatchMapping("/asistencia/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.ATTENDANCE_UPDATE + "')")
    public ResponseEntity<AsistenciaResponse> corregir(
        @PathVariable Long id,
        @Valid @RequestBody AsistenciaUpdateRequest request,
        Authentication authentication) {
        return ResponseEntity.ok(asistenciaService.corregir(
            id,
            request,
            authentication.getName()));
    }
}
