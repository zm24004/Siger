package com.siger.controller;

import com.siger.dto.EventoDisponibilidadResponse;
import com.siger.dto.EventoRecursoCantidadRequest;
import com.siger.dto.EventoRecursoRequest;
import com.siger.dto.EventoRecursoResponse;
import com.siger.dto.EventoRequest;
import com.siger.dto.EventoResponse;
import com.siger.dto.ReprogramarEventoRequest;
import com.siger.security.Permisos;
import com.siger.service.EventoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Public event catalogue and organizer-scoped event operations.
 */
@RestController
@Validated
@RequestMapping("/api/v1")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/eventos")
    public ResponseEntity<List<EventoResponse>> catalogo(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) @Positive Long idUbicacion
    ) {
        return ResponseEntity.ok(eventoService.catalogo(fechaDesde, fechaHasta, estado, idUbicacion));
    }

    @GetMapping("/eventos/{id}")
    public ResponseEntity<EventoResponse> detalle(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(eventoService.detalle(id));
    }

    @GetMapping("/eventos/{id}/disponibilidad")
    public ResponseEntity<EventoDisponibilidadResponse> disponibilidad(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(eventoService.disponibilidad(id));
    }

    @GetMapping("/eventos/mis-eventos")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "')")
    public ResponseEntity<List<EventoResponse>> misEventos() {
        return ResponseEntity.ok(eventoService.misEventos(SecurityUtils.requireEmail()));
    }

    @GetMapping("/organizadores/{id}/eventos")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "')")
    public ResponseEntity<List<EventoResponse>> eventosDeOrganizador(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(eventoService.eventosDeOrganizador(
            id,
            SecurityUtils.requireEmail(),
            SecurityUtils.hasAuthority(Permisos.USERS_READ)));
    }

    @PostMapping("/eventos")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "')")
    public ResponseEntity<EventoResponse> crear(@Valid @RequestBody EventoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventoService.crear(req, SecurityUtils.requireEmail()));
    }

    @PutMapping("/eventos/{id}")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "')")
    public ResponseEntity<EventoResponse> actualizar(
        @PathVariable @Positive Long id,
        @Valid @RequestBody EventoRequest req
    ) {
        return ResponseEntity.ok(eventoService.actualizar(id, req, SecurityUtils.requireEmail()));
    }

    @PatchMapping("/eventos/{id}/cancelar")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "')")
    public ResponseEntity<EventoResponse> cancelar(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(eventoService.cancelar(id, SecurityUtils.requireEmail()));
    }

    @PatchMapping("/eventos/{id}/reprogramar")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "')")
    public ResponseEntity<EventoResponse> reprogramar(
        @PathVariable @Positive Long id,
        @Valid @RequestBody ReprogramarEventoRequest req
    ) {
        return ResponseEntity.ok(eventoService.reprogramar(id, req, SecurityUtils.requireEmail()));
    }

    @PatchMapping("/eventos/{id}/finalizar")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "')")
    public ResponseEntity<EventoResponse> finalizar(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(eventoService.finalizar(id, SecurityUtils.requireEmail()));
    }


    /**
     * Endpoints para el manejo de recursos de eventos.
     */

    @GetMapping("/eventos/{id}/recursos")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "')")
    public ResponseEntity<List<EventoRecursoResponse>> listarRecursos(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(eventoService.listarRecursos(id, SecurityUtils.requireEmail()));
    }

    @PostMapping("/eventos/{id}/recursos")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "')")
    public ResponseEntity<EventoRecursoResponse> asignarRecurso(
        @PathVariable @Positive Long id,
        @Valid @RequestBody EventoRecursoRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventoService.asignarRecurso(id, req, SecurityUtils.requireEmail()));
    }

    @PutMapping("/eventos/{id}/recursos/{idRecurso}")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "')")
    public ResponseEntity<EventoRecursoResponse> actualizarRecurso(
        @PathVariable @Positive Long id,
        @PathVariable @Positive Long idRecurso,
        @Valid @RequestBody EventoRecursoCantidadRequest req
    ) {
        return ResponseEntity.ok(eventoService.actualizarRecurso(
            id, idRecurso, req.cantidadAsignada(), SecurityUtils.requireEmail()));
    }

    @DeleteMapping("/eventos/{id}/recursos/{idRecurso}")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "')")
    public ResponseEntity<Void> quitarRecurso(
        @PathVariable @Positive Long id,
        @PathVariable @Positive Long idRecurso
    ) {
        eventoService.quitarRecurso(id, idRecurso, SecurityUtils.requireEmail());
        return ResponseEntity.noContent().build();
    }
}
