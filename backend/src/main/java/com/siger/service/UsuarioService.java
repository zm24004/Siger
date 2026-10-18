package com.siger.service;

import com.siger.dto.UsuarioEstadoRequest;
import com.siger.dto.UsuarioResponse;
import com.siger.exception.NotFoundException;
import com.siger.model.Usuario;
import com.siger.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin-scoped user management: list all users, enable/disable accounts.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse detalle(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, UsuarioEstadoRequest req) {
        Usuario u = findById(id);
        u.setEstado(req.estado());
        Usuario saved = usuarioRepository.save(u);
        return toResponse(saved);
    }

    private Usuario findById(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
            u.getIdUsuario(),
            u.getNombre(),
            u.getApellido(),
            u.getEmail(),
            u.getTelefono(),
            u.getCodigoPais(),
            u.getEstado(),
            RolHelper.derivarRol(u),
            u.getFechaRegistro()
        );
    }
}
