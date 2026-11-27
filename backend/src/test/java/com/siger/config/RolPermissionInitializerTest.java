package com.siger.config;

import com.siger.model.Permiso;
import com.siger.model.Rol;
import com.siger.repository.PermisoRepository;
import com.siger.repository.RolRepository;
import com.siger.security.Permisos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolPermissionInitializerTest {

    @Mock RolRepository rolRepository;
    @Mock PermisoRepository permisoRepository;

    private final Map<String, Rol> roles = new HashMap<>();
    private final Map<String, Permiso> permisos = new HashMap<>();
    private RolPermissionInitializer initializer;

    @BeforeEach
    void setUp() {
        roles.put("ADM", rol("ADM", Set.of()));
        roles.put("ORG", rol("ORG", Set.of("obsolete:organizer")));
        roles.put("PAR", rol("PAR", Set.of("obsolete:participant")));
        roles.put("PUB", rol("PUB", Set.of()));

        when(rolRepository.existsById(anyString()))
            .thenAnswer(invocation -> roles.containsKey(invocation.getArgument(0)));
        when(rolRepository.findById(anyString()))
            .thenAnswer(invocation -> Optional.ofNullable(roles.get(invocation.getArgument(0))));
        when(rolRepository.save(any(Rol.class))).thenAnswer(invocation -> {
            Rol rol = invocation.getArgument(0);
            roles.put(rol.getIdRol(), rol);
            return rol;
        });
        when(permisoRepository.findById(anyString()))
            .thenAnswer(invocation -> Optional.ofNullable(permisos.get(invocation.getArgument(0))));
        when(permisoRepository.save(any(Permiso.class))).thenAnswer(invocation -> {
            Permiso permiso = invocation.getArgument(0);
            permisos.put(permiso.getIdPermiso(), permiso);
            return permiso;
        });
        when(permisoRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<String> ids = invocation.getArgument(0);
            List<Permiso> encontrados = new ArrayList<>();
            ids.forEach(id -> {
                Permiso permiso = permisos.get(id);
                if (permiso != null) {
                    encontrados.add(permiso);
                }
            });
            return encontrados;
        });

        initializer = new RolPermissionInitializer(rolRepository, permisoRepository);
    }

    @Test
    void sincronizaLosPermisosDeOrganizadorYParticipanteConLaMatrizPredeterminada() {
        initializer.run();
        initializer.run();

        assertThat(codigos("ORG")).containsExactlyInAnyOrder(
            Permisos.EVENTS_MANAGE,
            Permisos.LOCATIONS_READ, Permisos.LOCATIONS_CREATE, Permisos.LOCATIONS_UPDATE,
            Permisos.RESOURCES_READ, Permisos.RESOURCES_CREATE, Permisos.RESOURCES_UPDATE,
            Permisos.RESERVATIONS_READ_EVENT,
            Permisos.ATTENDANCE_REGISTER, Permisos.ATTENDANCE_READ, Permisos.ATTENDANCE_UPDATE);
        assertThat(codigos("PAR")).containsExactlyInAnyOrder(
            Permisos.RESERVATIONS_CREATE,
            Permisos.RESERVATIONS_READ_OWN,
            Permisos.RESERVATIONS_MANAGE_OWN);
    }

    private Rol rol(String codigo, Set<String> permisosIniciales) {
        Rol rol = new Rol();
        rol.setIdRol(codigo);
        rol.setNombre(codigo);
        rol.setPermisos(permisosIniciales.stream()
            .map(codigoPermiso -> new Permiso(codigoPermiso, codigoPermiso))
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        return rol;
    }

    private Set<String> codigos(String codigoRol) {
        return roles.get(codigoRol).getPermisos().stream()
            .map(Permiso::getIdPermiso)
            .collect(java.util.stream.Collectors.toSet());
    }
}
