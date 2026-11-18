package com.siger.controller;

import com.siger.dto.ReservacionRequest;
import com.siger.dto.ReservacionResponse;
import com.siger.security.Permisos;
import com.siger.service.ComprobantePdfService;
import com.siger.service.ReservacionService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ReservacionController {

    private final ReservacionService service;
    private final ComprobantePdfService comprobantePdfService;

    public ReservacionController(ReservacionService service, ComprobantePdfService comprobantePdfService) {
        this.service = service;
        this.comprobantePdfService = comprobantePdfService;
    }

    @PostMapping("/reservaciones")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_CREATE + "')")
    public ResponseEntity<ReservacionResponse> reservar(@Valid @RequestBody ReservacionRequest request,
                                                        Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.reservar(request.idEvento(), auth.getName()));
    }

    @PostMapping("/reservaciones/evento/{idEvento}")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_CREATE + "')")
    public ResponseEntity<ReservacionResponse> reservarAlias(@PathVariable Long idEvento, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.reservar(idEvento, auth.getName()));
    }

    @GetMapping("/reservaciones/mis-reservaciones")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_READ_OWN + "')")
    public ResponseEntity<List<ReservacionResponse>> misReservaciones(Authentication auth) {
        return ResponseEntity.ok(service.misReservaciones(auth.getName()));
    }

    @GetMapping("/reservaciones/{id}")
    @PreAuthorize("hasAnyAuthority('" + Permisos.RESERVATIONS_READ_OWN + "', '"
        + Permisos.RESERVATIONS_READ_EVENT + "', '" + Permisos.RESERVATIONS_READ_ALL + "')")
    public ResponseEntity<ReservacionResponse> detalle(@PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(service.detalle(
            id,
            auth.getName(),
            SecurityUtils.hasAuthority(Permisos.RESERVATIONS_READ_ALL),
            SecurityUtils.hasAuthority(Permisos.RESERVATIONS_READ_EVENT)));
    }

    @GetMapping("/reservaciones/{id}/comprobante")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_READ_OWN + "')")
    public ResponseEntity<byte[]> comprobante(@PathVariable Long id, Authentication auth) {
        ReservacionResponse reservacion = service.comprobante(id, auth.getName());
        byte[] pdf = comprobantePdfService.generar(reservacion);
        ContentDisposition disposition = ContentDisposition.attachment()
            .filename("comprobante-" + reservacion.codigoReservacion() + ".pdf", StandardCharsets.UTF_8)
            .build();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(pdf);
    }

    @PatchMapping("/reservaciones/{id}/pausar")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_MANAGE_OWN + "')")
    public ResponseEntity<ReservacionResponse> pausar(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.pausar(id, auth.getName()));
    }

    @PatchMapping("/reservaciones/{id}/confirmar")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_MANAGE_OWN + "')")
    public ResponseEntity<ReservacionResponse> confirmar(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.confirmar(id, auth.getName()));
    }

    @PatchMapping("/reservaciones/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('" + Permisos.RESERVATIONS_MANAGE_OWN + "', '"
        + Permisos.EVENTS_MANAGE + "', '" + Permisos.RESERVATIONS_MANAGE_ALL + "')")
    public ResponseEntity<ReservacionResponse> cancelar(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.cancelar(
            id,
            auth.getName(),
            SecurityUtils.hasAuthority(Permisos.RESERVATIONS_MANAGE_ALL),
            SecurityUtils.hasAuthority(Permisos.EVENTS_MANAGE)));
    }

    @GetMapping("/reservaciones")
    @PreAuthorize("hasAnyAuthority('" + Permisos.RESERVATIONS_READ_ALL + "', '"
        + Permisos.RESERVATIONS_READ_EVENT + "')")
    public ResponseEntity<List<ReservacionResponse>> listar(
        @RequestParam(required = false) Long idEvento,
        @RequestParam(required = false) String estado,
        Authentication auth) {
        return ResponseEntity.ok(service.listar(
            idEvento,
            estado,
            auth.getName(),
            SecurityUtils.hasAuthority(Permisos.RESERVATIONS_READ_ALL),
            SecurityUtils.hasAuthority(Permisos.RESERVATIONS_READ_EVENT)));
    }

    @GetMapping("/eventos/{idEvento}/reservaciones")
    @PreAuthorize("hasAnyAuthority('" + Permisos.RESERVATIONS_READ_ALL + "', '"
        + Permisos.RESERVATIONS_READ_EVENT + "')")
    public ResponseEntity<List<ReservacionResponse>> listarPorEvento(
        @PathVariable Long idEvento, Authentication auth) {
        return ResponseEntity.ok(service.listarPorEvento(
            idEvento,
            auth.getName(),
            SecurityUtils.hasAuthority(Permisos.RESERVATIONS_READ_ALL)));
    }

    @GetMapping("/participantes/{idParticipante}/reservaciones")
    @PreAuthorize("hasAnyAuthority('" + Permisos.RESERVATIONS_READ_OWN + "', '"
        + Permisos.RESERVATIONS_READ_ALL + "')")
    public ResponseEntity<List<ReservacionResponse>> historialParticipante(
        @PathVariable Long idParticipante, Authentication auth) {
        return ResponseEntity.ok(service.historialParticipante(
            idParticipante,
            auth.getName(),
            SecurityUtils.hasAuthority(Permisos.RESERVATIONS_READ_ALL)));
    }

    @PatchMapping("/reservaciones/admin/{id}/cancelar")
    @PreAuthorize("hasAuthority('" + Permisos.RESERVATIONS_MANAGE_ALL + "')")
    public ResponseEntity<ReservacionResponse> cancelarComoAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelarComoAdmin(id));
    }
}
