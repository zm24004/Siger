package com.siger.service;

import com.siger.dto.ActualizarPermisosRequest;
import com.siger.dto.PermisoResponse;
import com.siger.dto.RolResponse;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Permiso;
import com.siger.model.Rol;
import com.siger.repository.PermisoRepository;
import com.siger.repository.RolRepository;
import com.siger.security.Permisos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    @Transactional(readOnly = true)
    public List<RolResponse> listar() {
        return rolRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public RolResponse actualizarPermisos(String id, ActualizarPermisosRequest request) {
        Rol rol = rolRepository.findById(id.toUpperCase())
            .orElseThrow(() -> new NotFoundException("Rol no encontrado: " + id));
        Set<String> ids = request.permisos();

        if ("ADM".equals(rol.getIdRol()) && !ids.contains(Permisos.ROLES_UPDATE)) {
            throw new BusinessException("El rol ADM debe conservar el permiso roles:update.");
        }

        List<Permiso> permisos = permisoRepository.findAllById(ids);
        if (permisos.size() != ids.size()) {
            Set<String> existentes = permisos.stream().map(Permiso::getIdPermiso).collect(Collectors.toSet());
            Set<String> desconocidos = ids.stream().filter(idPermiso -> !existentes.contains(idPermiso)).collect(Collectors.toSet());
            throw new BusinessException("Permisos desconocidos: " + desconocidos);
        }

        rol.setPermisos(permisos.stream().collect(Collectors.toSet()));
        return toResponse(rolRepository.save(rol));
    }

    private RolResponse toResponse(Rol rol) {
        return new RolResponse(
            rol.getIdRol(),
            rol.getNombre(),
            rol.getPermisos().stream()
                .map(permiso -> new PermisoResponse(permiso.getIdPermiso(), permiso.getDescripcion()))
                .sorted(java.util.Comparator.comparing(PermisoResponse::idPermiso))
                .toList());
    }
}
