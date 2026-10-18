package com.siger.controller;

import com.siger.dto.NotificacionResponse;
import com.siger.exception.NotFoundException;
import com.siger.model.Usuario;
import com.siger.repository.UsuarioRepository;
import com.siger.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User-scoped notification endpoints. Any authenticated user can access.
 * The idUsuario is resolved from the JWT subject (email) via UsuarioRepository.
 */
@RestController
@RequestMapping("/api/notificaciones")
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

    @GetMapping("/mis/no-leidas")
    public ResponseEntity<Map<String, Long>> noLeidas() {
        long n = notificacionService.contarNoLeidas(currentIdUsuario());
        return ResponseEntity.ok(Map.of("noLeidas", n));
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<NotificacionResponse> marcarLeida(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.marcarLeida(id));
    }

    private Long currentIdUsuario() {
        String email = SecurityUtils.requireEmail();
        Usuario u = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado: " + email));
        return u.getIdUsuario();
    }
}
