package com.siger.service;

import com.siger.dto.CambiarContrasenaRequest;
import com.siger.dto.UsuarioResponse;
import com.siger.dto.auth.AuthResponse;
import com.siger.dto.auth.LoginRequest;
import com.siger.dto.auth.RegistroRequest;
import com.siger.exception.BusinessException;
import com.siger.model.Participante;
import com.siger.model.Usuario;
import com.siger.repository.ParticipanteRepository;
import com.siger.repository.UsuarioRepository;
import com.siger.security.JwtService;
import com.siger.security.TokenRevocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ParticipanteRepository participanteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new BusinessException("Credenciales inválidas."));
        if (!Boolean.TRUE.equals(usuario.getEstado())
            || !passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
            throw new BusinessException("Credenciales inválidas.");
        }

        String rol = RolHelper.derivarRol(usuario);
        String token = jwtService.generar(usuario.getEmail(), rol);
        return new AuthResponse(token, usuario.getNombre(), usuario.getApellido(), rol, usuario.getIdUsuario());
    }

    @Transactional
    public AuthResponse registro(RegistroRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Ya existe una cuenta con ese email.");
        }

        Participante participante = new Participante();
        participante.setNombre(request.nombre().trim());
        participante.setApellido(request.apellido() == null ? "" : request.apellido().trim());
        participante.setEmail(email);
        participante.setTelefono(request.telefono() == null ? "" : request.telefono().trim());
        participante.setCodigoPais(request.codigoPais() == null ? "" : request.codigoPais().trim());
        participante.setContrasena(passwordEncoder.encode(request.contrasena()));
        participante.setEstado(true);

        Participante nuevo = participanteRepository.save(participante);
        String token = jwtService.generar(nuevo.getEmail(), "PARTICIPANTE");
        return new AuthResponse(token, nuevo.getNombre(), nuevo.getApellido(), "PARTICIPANTE", nuevo.getIdUsuario());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .filter(u -> Boolean.TRUE.equals(u.getEstado()))
            .orElseThrow(() -> new BusinessException("La cuenta autenticada ya no está activa."));
        return toResponse(usuario);
    }

    @Transactional
    public void cambiarContrasena(String email, CambiarContrasenaRequest request, String token) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .filter(u -> Boolean.TRUE.equals(u.getEstado()))
            .orElseThrow(() -> new BusinessException("La cuenta autenticada ya no está activa."));
        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getContrasena())) {
            throw new BusinessException("La contraseña actual es incorrecta.");
        }
        if (passwordEncoder.matches(request.contrasenaNueva(), usuario.getContrasena())) {
            throw new BusinessException("La contraseña nueva debe ser diferente a la actual.");
        }

        usuario.setContrasena(passwordEncoder.encode(request.contrasenaNueva()));
        usuarioRepository.save(usuario);
        tokenRevocationService.revocar(token);
    }

    @Transactional
    public void logout(String token) {
        tokenRevocationService.revocar(token);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getIdUsuario(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            usuario.getTelefono(),
            usuario.getCodigoPais(),
            usuario.getEstado(),
            RolHelper.derivarRol(usuario),
            usuario.getFechaRegistro());
    }
}
