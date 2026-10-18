package com.siger.service;

import com.siger.dto.auth.AuthResponse;
import com.siger.dto.auth.LoginRequest;
import com.siger.dto.auth.RegistroRequest;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Usuario;
import com.siger.repository.AdministradorRepository;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.ParticipanteRepository;
import com.siger.repository.UsuarioRepository;
import com.siger.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user login and self-registration (Organizador / Participante).
 * Self-registration of ADMINISTRADOR is not allowed.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ParticipanteRepository participanteRepository;
    private final OrganizadorRepository organizadorRepository;
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarioRepository.findByEmail(req.email())
            .orElseThrow(() -> new NotFoundException("Credenciales inválidas."));

        if (Boolean.FALSE.equals(u.getEstado())) {
            throw new BusinessException("La cuenta está desactivada. Contacta al administrador.");
        }

        if (!passwordEncoder.matches(req.contrasena(), u.getContrasena())) {
            throw new BusinessException("Credenciales inválidas.");
        }

        String rol = RolHelper.derivarRol(u);
        String token = jwtService.generar(u.getEmail(), rol);
        return new AuthResponse(token, u.getNombre(), u.getApellido(), rol, u.getIdUsuario());
    }

    @Transactional
    public AuthResponse registro(RegistroRequest req) {
        String rol = req.rol() == null ? "" : req.rol().trim().toUpperCase();
        if (rol.equals("ADMINISTRADOR")) {
            throw new BusinessException("No se permite el registro de administradores. Contacta a un administrador.");
        }
        if (!rol.equals("ORGANIZADOR") && !rol.equals("PARTICIPANTE")) {
            throw new BusinessException("Rol inválido. Debe ser ORGANIZADOR o PARTICIPANTE.");
        }
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new BusinessException("Ya existe una cuenta con el email: " + req.email());
        }

        String hashed = passwordEncoder.encode(req.contrasena());

        Usuario nuevo;
        if (rol.equals("ORGANIZADOR")) {
            Organizador o = new Organizador();
            applyCommonFields(o, req, hashed);
            nuevo = organizadorRepository.save(o);
        } else {
            Participante p = new Participante();
            applyCommonFields(p, req, hashed);
            nuevo = participanteRepository.save(p);
        }

        String token = jwtService.generar(nuevo.getEmail(), rol);
        return new AuthResponse(token, nuevo.getNombre(), nuevo.getApellido(), rol, nuevo.getIdUsuario());
    }

    private void applyCommonFields(Usuario u, RegistroRequest req, String hashed) {
        u.setNombre(req.nombre());
        u.setApellido(req.apellido());
        u.setEmail(req.email());
        u.setTelefono(req.telefono());
        u.setCodigoPais(req.codigoPais());
        u.setContrasena(hashed);
        u.setEstado(true);
    }
}
