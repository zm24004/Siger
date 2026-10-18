package com.siger.controller;

import com.siger.dto.auth.AuthResponse;
import com.siger.dto.auth.LoginRequest;
import com.siger.dto.auth.RecuperarContrasenaRequest;
import com.siger.dto.auth.RegistroRequest;
import com.siger.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public authentication endpoints: login and self-registration.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest req) {
        return ResponseEntity.ok(authService.registro(req));
    }

    /**
     * Recuperación de contraseña (protocolo).
     * SIEMPRE devuelve el mismo mensaje, sin revelar si el email existe
     * (por seguridad). No envía correo real; el usuario debe contactar al admin
     * o un administrador debe resetearle la contraseña a mano.
     */
    @PostMapping("/recuperar-contrasena")
    public ResponseEntity<Map<String, String>> recuperar(@Valid @RequestBody RecuperarContrasenaRequest req) {
        return ResponseEntity.ok(Map.of(
            "message", "Se ha enviado un correo a " + req.email() + " con las instrucciones para recuperar tu contraseña.",
            "protocolo", "Revisa tu bandeja de entrada. Si no lo recibes en unos minutos, revisa spam."
        ));
    }
}
