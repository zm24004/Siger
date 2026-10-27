package com.siger.config;

import com.siger.model.Permiso;
import com.siger.model.Rol;
import com.siger.repository.PermisoRepository;
import com.siger.repository.RolRepository;
import com.siger.security.Permisos;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RolPermissionInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, String> catalogo = new LinkedHashMap<>();
        catalogo.put(Permisos.USERS_READ, "Consultar usuarios");
        catalogo.put(Permisos.USERS_CREATE, "Crear usuarios");
        catalogo.put(Permisos.USERS_UPDATE, "Actualizar usuarios");
        catalogo.put(Permisos.USERS_STATE, "Activar o desactivar usuarios");
        catalogo.put(Permisos.USERS_DELETE, "Eliminar usuarios lógicamente");
        catalogo.put(Permisos.ROLES_READ, "Consultar roles y permisos");
        catalogo.put(Permisos.ROLES_UPDATE, "Configurar permisos por rol");
        catalogo.put(Permisos.EVENTS_MANAGE, "Administrar eventos propios");
        catalogo.put(Permisos.LOCATIONS_READ, "Consultar ubicaciones");
        catalogo.put(Permisos.LOCATIONS_CREATE, "Crear ubicaciones");
        catalogo.put(Permisos.LOCATIONS_UPDATE, "Actualizar ubicaciones");
        catalogo.put(Permisos.LOCATIONS_DELETE, "Eliminar ubicaciones");
        catalogo.put(Permisos.RESOURCES_READ, "Consultar recursos");
        catalogo.put(Permisos.RESOURCES_CREATE, "Crear recursos");
        catalogo.put(Permisos.RESOURCES_UPDATE, "Actualizar recursos");
        catalogo.put(Permisos.RESOURCES_DELETE, "Eliminar recursos");
        catalogo.put(Permisos.ATTENDANCE_REGISTER, "Registrar asistencia");
        catalogo.put(Permisos.ATTENDANCE_READ, "Consultar asistencia");
        catalogo.put(Permisos.ATTENDANCE_UPDATE, "Corregir asistencia");
        catalogo.put(Permisos.RESERVATIONS_CREATE, "Crear reservaciones");
        catalogo.put(Permisos.RESERVATIONS_READ_OWN, "Consultar reservaciones propias");
        catalogo.put(Permisos.RESERVATIONS_MANAGE_OWN, "Gestionar reservaciones propias");
        catalogo.put(Permisos.RESERVATIONS_READ_EVENT, "Consultar reservaciones de eventos propios");
        catalogo.put(Permisos.RESERVATIONS_READ_ALL, "Consultar todas las reservaciones");
        catalogo.put(Permisos.RESERVATIONS_MANAGE_ALL, "Gestionar todas las reservaciones");

        catalogo.forEach((codigo, descripcion) ->
            permisoRepository.findById(codigo).orElseGet(() ->
                permisoRepository.save(new Permiso(codigo, descripcion))));

        crearRol("ADM", "Administrador", catalogo.keySet());
        sincronizarPermisosAdministrador(catalogo.keySet());
        Set<String> permisosOrganizador = Set.of(
            Permisos.EVENTS_MANAGE,
            Permisos.LOCATIONS_READ, Permisos.LOCATIONS_CREATE, Permisos.LOCATIONS_UPDATE,
            Permisos.RESOURCES_READ, Permisos.RESOURCES_CREATE, Permisos.RESOURCES_UPDATE,
            Permisos.RESERVATIONS_READ_EVENT,
            Permisos.ATTENDANCE_REGISTER, Permisos.ATTENDANCE_READ, Permisos.ATTENDANCE_UPDATE);
        crearRol("ORG", "Organizador", permisosOrganizador);
        sincronizarPermisosOrganizador(permisosOrganizador);
        Set<String> permisosParticipante = Set.of(
            Permisos.RESERVATIONS_CREATE, Permisos.RESERVATIONS_READ_OWN,
            Permisos.RESERVATIONS_MANAGE_OWN);
        crearRol("PAR", "Participante", permisosParticipante);
        sincronizarPermisosParticipante(permisosParticipante);
        crearRol("PUB", "Público", Set.of());
    }

    private void crearRol(String codigo, String nombre, Set<String> permisosPorDefecto) {
        if (rolRepository.existsById(codigo)) {
            return;
        }

        Rol rol = new Rol();
        rol.setIdRol(codigo);
        rol.setNombre(nombre);
        rol.setPermisos(permisoRepository.findAllById(permisosPorDefecto)
            .stream()
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)));
        rolRepository.save(rol);
    }

    private void sincronizarPermisosAdministrador(Set<String> todosLosPermisos) {
        Rol administrador = rolRepository.findById("ADM")
            .orElseThrow(() -> new IllegalStateException("No se pudo inicializar el rol ADM"));
        administrador.setPermisos(permisoRepository.findAllById(todosLosPermisos)
            .stream()
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)));
        rolRepository.save(administrador);
    }

    private void sincronizarPermisosOrganizador(Set<String> permisosPorDefecto) {
        Rol organizador = obtenerRol("ORG");
        organizador.setPermisos(permisoRepository.findAllById(permisosPorDefecto)
            .stream()
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)));
        rolRepository.save(organizador);
    }

    private void sincronizarPermisosParticipante(Set<String> permisosPorDefecto) {
        Rol participante = obtenerRol("PAR");
        participante.setPermisos(permisoRepository.findAllById(permisosPorDefecto)
            .stream()
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)));
        rolRepository.save(participante);
    }

    private Rol obtenerRol(String codigo) {
        return rolRepository.findById(codigo)
            .orElseThrow(() -> new IllegalStateException("No se pudo inicializar el rol " + codigo));
    }
}
