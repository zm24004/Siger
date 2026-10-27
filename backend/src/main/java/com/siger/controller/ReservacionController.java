package com.siger.controller;

import com.siger.dto.ReservacionResponse;
import com.siger.security.Permisos;
import com.siger.service.ReservacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservaciones")
public class ReservacionController {

    private final ReservacionService service;

    public ReservacionController(ReservacionService service) {
        this.service = service;
    }

    @PostMapping("/evento/{idEvento}")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_CREATE + "')")
    public ResponseEntity<ReservacionResponse> reservar(@PathVariable Long idEvento, Authentication auth) {
        return ResponseEntity.ok(service.reservar(idEvento, auth.getName()));
    }

    @GetMapping("/mis-reservaciones")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_READ_OWN + "')")
    public ResponseEntity<List<ReservacionResponse>> misReservaciones(Authentication auth) {
        return ResponseEntity.ok(service.misReservaciones(auth.getName()));
    }

    @GetMapping("/{codigo}")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_READ_OWN + "')")
    public ResponseEntity<ReservacionResponse> detalle(@PathVariable String codigo) {
        return ResponseEntity.ok(service.detallePorCodigo(codigo));
    }

    @PatchMapping("/{id}/pausar")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_MANAGE_OWN + "')")
    public ResponseEntity<ReservacionResponse> pausar(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.pausar(id, auth.getName()));
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_MANAGE_OWN + "')")
    public ResponseEntity<ReservacionResponse> confirmar(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.confirmar(id, auth.getName()));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_MANAGE_OWN + "')")
    public ResponseEntity<ReservacionResponse> cancelar(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.cancelar(id, auth.getName()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_READ_ALL + "')")
    public ResponseEntity<List<ReservacionResponse>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @PatchMapping("/admin/{id}/cancelar")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_MANAGE_ALL + "')")
    public ResponseEntity<ReservacionResponse> cancelarComoAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelarComoAdmin(id));
    }
}
