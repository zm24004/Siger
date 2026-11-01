package com.siger.controller;

import com.siger.dto.NotificacionCursor;
import com.siger.dto.NotificacionEnvioRequest;
import com.siger.dto.NotificacionEnvioResponse;
import com.siger.dto.NotificacionPageResponse;
import com.siger.dto.NotificacionResponse;
import com.siger.exception.NotFoundException;
import com.siger.model.Usuario;
import com.siger.repository.UsuarioRepository;
import com.siger.security.Permisos;
import com.siger.service.NotificacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * User-scoped notification endpoints. Any authenticated user can access.
 * The idUsuario is resolved from the JWT subject (email) via UsuarioRepository.
 */
@RestController
@Validated
@RequestMapping("/api/v1/notificaciones")
@PreAuthorize("isAuthenticated()")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;

    public NotificacionController(NotificacionService notificacionService,
                                  UsuarioRepository usuarioRepository) {
        this.notificacionService = notificacionService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/mis")
    public ResponseEntity<List<NotificacionResponse>> mis() {
        return ResponseEntity.ok(notificacionService.listarPorUsuario(currentIdUsuario()));
    }

    @GetMapping("/mis/pagina")
    public ResponseEntity<NotificacionPageResponse> misPagina(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorFecha,
        @RequestParam(required = false) @Positive Long cursorId,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit
    ) {
        if ((cursorFecha == null) != (cursorId == null)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "cursorFecha y cursorId deben enviarse juntos.");
        }
        NotificacionCursor cursor = cursorFecha == null
            ? null
            : new NotificacionCursor(cursorFecha, cursorId);
        return ResponseEntity.ok(
            notificacionService.listarPaginaPorUsuario(currentIdUsuario(), cursor, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificacionResponse> obtener(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(notificacionService.obtener(id, currentIdUsuario()));
    }

    @GetMapping("/mis/no-leidas")
    public ResponseEntity<Map<String, Long>> noLeidas() {
        long n = notificacionService.contarNoLeidas(currentIdUsuario());
        return ResponseEntity.ok(Map.of("noLeidas", n));
    }

    @PatchMapping({"/{id}/leida", "/{id}/leer"})
    public ResponseEntity<NotificacionResponse> marcarLeida(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(notificacionService.marcarLeida(id, currentIdUsuario()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        notificacionService.eliminar(id, currentIdUsuario());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/enviar")
    @PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "') or hasAuthority('" + Permisos.USERS_READ + "')")
    public ResponseEntity<NotificacionEnvioResponse> enviarRecordatorio(
        @Valid @RequestBody NotificacionEnvioRequest request
    ) {
        boolean administrador = SecurityUtils.hasAuthority(Permisos.USERS_READ);
        NotificacionEnvioResponse response = notificacionService.enviarRecordatorio(
            request, SecurityUtils.requireEmail(), administrador);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Long currentIdUsuario() {
        String email = SecurityUtils.requireEmail();
        Usuario u = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado: " + email));
        return u.getIdUsuario();
    }
}
