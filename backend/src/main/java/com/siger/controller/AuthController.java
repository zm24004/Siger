package com.siger.controller;

import com.siger.dto.CambiarContrasenaRequest;
import com.siger.dto.UsuarioResponse;
import com.siger.dto.auth.AuthResponse;
import com.siger.dto.auth.LoginRequest;
import com.siger.dto.auth.RecuperarContrasenaRequest;
import com.siger.dto.auth.RegistroRequest;
import com.siger.exception.BusinessException;
import com.siger.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(201).body(authService.registro(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(bearerToken(authorization));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
        @Valid @RequestBody RecuperarContrasenaRequest request
    ) {
        return ResponseEntity.ok(Map.of(
            "message", "Si existe una cuenta asociada, recibirás instrucciones para recuperar el acceso."
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.perfil(authentication.getName()));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> cambiarContrasena(
        Authentication authentication,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @Valid @RequestBody CambiarContrasenaRequest request
    ) {
        authService.cambiarContrasena(authentication.getName(), request, bearerToken(authorization));
        return ResponseEntity.noContent().build();
    }

    private String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7) {
            throw new BusinessException("Se requiere un token Bearer válido.");
        }
        return authorization.substring(7);
    }
}
