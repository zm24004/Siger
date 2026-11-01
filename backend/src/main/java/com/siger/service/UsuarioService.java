package com.siger.service;

import com.siger.dto.UsuarioCreateRequest;
import com.siger.dto.UsuarioEstadoRequest;
import com.siger.dto.UsuarioPageResponse;
import com.siger.dto.UsuarioResponse;
import com.siger.dto.UsuarioUpdateRequest;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Administrador;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Usuario;
import com.siger.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UsuarioPageResponse listar(String rol, Boolean estado, int page, int limit) {
        // Inicializamos con una especificación limpia que siempre es verdadera (conjunction)
        Specification<Usuario> specification = (root, query, builder) -> builder.conjunction();
        
        if (estado != null) {
            specification = specification.and((root, query, builder) -> 
                builder.equal(root.get("estado"), estado)
            );
        }
        
        if (rol != null && !rol.isBlank()) {
            Class<? extends Usuario> tipo = tipoUsuario(rol);
            
            if (tipo != null) {
                specification = specification.and((root, query, builder) -> 
                    builder.equal(root.type(), tipo)
                );
            }
        }

        // Prevenimos que page-1 sea negativo (Spring Data requiere que el índice empiece en 0)
        int pageIndex = Math.max(0, page - 1);
        Page<Usuario> usuarios = usuarioRepository.findAll(specification, PageRequest.of(pageIndex, limit));
        
        return new UsuarioPageResponse(
            usuarios.getContent().stream().map(this::toResponse).toList(),
            page,
            limit,
            usuarios.getTotalElements(),
            usuarios.getTotalPages()
        );
    }


    @Transactional(readOnly = true)
    public UsuarioResponse detalle(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado."));
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Ya existe una cuenta con ese email.");
        }

        Usuario nuevo = switch (normalizarRol(request.rol())) {
            case "ADM" -> new Administrador();
            case "ORG" -> new Organizador();
            case "PAR" -> new Participante();
            default -> throw new BusinessException("Rol inválido. Use ADM, ORG o PAR.");
        };
        nuevo.setNombre(request.nombre().trim());
        nuevo.setApellido(request.apellido().trim());
        nuevo.setEmail(email);
        nuevo.setTelefono(request.telefono().trim());
        nuevo.setCodigoPais(request.codigoPais().trim());
        nuevo.setContrasena(passwordEncoder.encode(request.contrasena()));
        nuevo.setEstado(true);
        return toResponse(usuarioRepository.save(nuevo));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = findById(id);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmailAndIdUsuarioNot(email, id)) {
            throw new BusinessException("Ya existe una cuenta con ese email.");
        }
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setEmail(email);
        usuario.setTelefono(request.telefono().trim());
        usuario.setCodigoPais(request.codigoPais().trim());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, UsuarioEstadoRequest request) {
        Usuario usuario = findById(id);
        usuario.setEstado(request.estado());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = findById(id);
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
    }

    private Usuario findById(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
    }

    private Class<? extends Usuario> tipoUsuario(String rol) {
        return switch (normalizarRol(rol)) {
            case "ADM" -> Administrador.class;
            case "ORG" -> Organizador.class;
            case "PAR" -> Participante.class;
            default -> throw new BusinessException("Rol inválido. Use ADM, ORG o PAR.");
        };
    }

    private String normalizarRol(String rol) {
        return switch (rol.trim().toUpperCase(Locale.ROOT)) {
            case "ADM", "ADMINISTRADOR" -> "ADM";
            case "ORG", "ORGANIZADOR" -> "ORG";
            case "PAR", "PARTICIPANTE" -> "PAR";
            default -> rol.trim().toUpperCase(Locale.ROOT);
        };
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
