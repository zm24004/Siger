package com.siger.controller;

import com.siger.dto.EventoRequest;
import com.siger.dto.EventoResponse;
import com.siger.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Event catalogue (public read) + organizador-scoped CRUD.
 */
@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    public ResponseEntity<List<EventoResponse>> catalogo() {
        return ResponseEntity.ok(eventoService.catalogoActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.detalle(id));
    }

    @GetMapping("/mis-eventos")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<List<EventoResponse>> misEventos() {
        return ResponseEntity.ok(eventoService.misEventos(SecurityUtils.requireEmail()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<EventoResponse> crear(@Valid @RequestBody EventoRequest req) {
        return ResponseEntity.ok(eventoService.crear(req, SecurityUtils.requireEmail()));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<EventoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.cancelar(id));
    }
}
