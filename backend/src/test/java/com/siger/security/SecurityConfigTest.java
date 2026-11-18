package com.siger.security;

import com.siger.config.SecurityConfig;
import com.siger.controller.EventoController;
import com.siger.controller.RecursoController;
import com.siger.controller.UbicacionController;
import com.siger.controller.UsuarioController;
import com.siger.dto.EventoRequest;
import com.siger.dto.EventoResponse;
import com.siger.dto.PageResponse;
import com.siger.dto.UsuarioPageResponse;
import com.siger.model.Administrador;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Permiso;
import com.siger.model.Rol;
import com.siger.model.Usuario;
import com.siger.repository.RolRepository;
import com.siger.repository.UsuarioRepository;
import com.siger.service.EventoService;
import com.siger.service.RecursoService;
import com.siger.service.UbicacionService;
import com.siger.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la configuracion de seguridad ({@link SecurityConfig}) usando
 * MockMvc con el slice {@code @WebMvcTest}.
 *
 * <p>Se cargan los controladores reales ({@link EventoController},
 * {@link UsuarioController}, {@link UbicacionController} y {@link RecursoController})
 * junto con la cadena de filtros de Spring Security
 * ({@code SecurityFilterChain}, {@link JwtFilter}, {@link JwtService},
 * {@link UserDetailsServiceImpl} y los handlers 401/403). Los servicios y
 * {@link UsuarioRepository} se reemplazan por mocks de Mockito.
 *
 * <p>El secreto JWT y el origen CORS provienen de
 * {@code application-test.properties} (perfil "test"); no se usa base de datos.
 *
 * <p>Cubre: rutas publicas (GET /api/v1/eventos, GET /api/v1/eventos/{id}), rutas
 * protegidas sin token (401), token invalido (401), control de roles con
 * {@code @PreAuthorize} (PARTICIPANTE 403, ORGANIZADOR 200, ADMINISTRADOR 200)
 * y validacion de Bean Validation (400) tras pasar la autorizacion.
 */
@WebMvcTest(controllers = {
        EventoController.class, UsuarioController.class, UbicacionController.class, RecursoController.class
})
@Import({
    SecurityConfig.class,
    JwtFilter.class,
    JwtService.class,
    UserDetailsServiceImpl.class,
    AuthEntryPointJwt.class,
    AccessDeniedHandlerJwt.class
})
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;

    @MockitoBean EventoService eventoService;
    @MockitoBean UsuarioService usuarioService;
    @MockitoBean UbicacionService ubicacionService;
    @MockitoBean RecursoService recursoService;
    @MockitoBean UsuarioRepository usuarioRepository;
    @MockitoBean RolRepository rolRepository;
    @MockitoBean TokenRevocationService tokenRevocationService;

    // ------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------

    private String tokenPara(String email, String rol) {
        return jwtService.generarToken(email, rol);
    }

    /** Hace que UserDetailsServiceImpl encuentre al usuario por email. */
    private void usuarioExiste(String email, String tipo) {
        usuarioExiste(email, tipo, true);
    }

    private void usuarioExiste(String email, String tipo, boolean activo) {
        Usuario user = switch (tipo) {
            case "ADMINISTRADOR" -> new Administrador();
            case "ORGANIZADOR" -> new Organizador();
            default -> new Participante();
        };
        user.setEmail(email);
        user.setNombre("User " + email);
        user.setContrasena("hash-bcrypt");
        user.setEstado(activo);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(user));

        String codigoRol = switch (tipo) {
            case "ADMINISTRADOR" -> "ADM";
            case "ORGANIZADOR" -> "ORG";
            default -> "PAR";
        };
        Set<String> permisos = switch (tipo) {
            case "ADMINISTRADOR" -> Set.of(Permisos.USERS_READ, Permisos.USERS_CREATE,
                    Permisos.USERS_UPDATE, Permisos.USERS_STATE, Permisos.USERS_DELETE,
                    Permisos.ROLES_READ, Permisos.ROLES_UPDATE,
                    Permisos.EVENTS_MANAGE,
                    Permisos.RESERVATIONS_READ_ALL, Permisos.RESERVATIONS_MANAGE_ALL,
                    Permisos.LOCATIONS_READ, Permisos.LOCATIONS_CREATE,
                    Permisos.LOCATIONS_UPDATE, Permisos.LOCATIONS_DELETE,
                    Permisos.RESOURCES_READ, Permisos.RESOURCES_CREATE,
                    Permisos.RESOURCES_UPDATE, Permisos.RESOURCES_DELETE);
            case "ORGANIZADOR" -> Set.of(Permisos.EVENTS_MANAGE, Permisos.LOCATIONS_CREATE,
                    Permisos.LOCATIONS_READ, Permisos.LOCATIONS_UPDATE,
                    Permisos.RESOURCES_READ, Permisos.RESOURCES_CREATE, Permisos.RESOURCES_UPDATE,
                    Permisos.ATTENDANCE_REGISTER);
            default -> Set.of(Permisos.RESERVATIONS_CREATE, Permisos.RESERVATIONS_READ_OWN,
                    Permisos.RESERVATIONS_MANAGE_OWN);
        };
        Rol rol = new Rol();
        rol.setIdRol(codigoRol);
        rol.setNombre(tipo);
        rol.setPermisos(permisos.stream()
                .map(permiso -> new Permiso(permiso, permiso))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        when(rolRepository.findById(codigoRol)).thenReturn(Optional.of(rol));
    }

    private EventoResponse eventoResponse() {
        return new EventoResponse(
                1L, "Evento test", "desc", LocalDate.of(2099, 1, 1),
                LocalTime.of(9, 0), LocalTime.of(11, 0), 50, "Activo",
                1L, "Auditorio", 1L, "Org", 50);
    }

    /** Body de POST /api/v1/eventos valido (pasa @Valid). */
    private static final String BODY_VALIDO = """
            {
              "nombre": "Evento test",
              "descripcion": "desc",
              "fecha": "2099-01-01",
              "horaInicio": "09:00:00",
              "horaFin": "11:00:00",
              "capacidadMaxima": 50,
              "idUbicacion": 1
            }
            """;

    /** Body de POST /api/v1/eventos invalido (violaciones de @NotBlank/@NotNull/@Positive). */
    private static final String BODY_INVALIDO = """
            {"descripcion":"sin nombre","capacidadMaxima":0,"idUbicacion":1}
            """;

    // ------------------------------------------------------------------
    //  Rutas publicas (permitAll)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/eventos sin token -> 200 (permitAll)")
    void getEventos_sinToken_retorna200() throws Exception {
        when(eventoService.catalogo(null, null, null, null, 1, 20))
            .thenReturn(new PageResponse<>(List.of(), 1, 20, 0, 0));

        mockMvc.perform(get("/api/v1/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/eventos/{id} sin token -> 200 (permitAll)")
    void getEventoPorId_sinToken_retorna200() throws Exception {
        when(eventoService.detalle(1L)).thenReturn(eventoResponse());

        mockMvc.perform(get("/api/v1/eventos/1"))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------
    //  Rutas protegidas sin token -> 401
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/eventos sin token -> 401 (AuthEntryPointJwt)")
    void postEventos_sinToken_retorna401() throws Exception {
        mockMvc.perform(post("/api/v1/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/usuarios sin token -> 401")
    void getUsuarios_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/ubicaciones y /api/v1/recursos sin token -> 401")
    void getCatalogos_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/ubicaciones"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/recursos"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    //  Token invalido -> 401
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/eventos con token invalido -> 401")
    void postEventos_conTokenInvalido_retorna401() throws Exception {
        mockMvc.perform(post("/api/v1/eventos")
                        .header("Authorization", "Bearer cadena-que-no-es-un-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    //  Control de roles (@PreAuthorize)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("el permiso se obtiene del rol actual, no del claim rol del JWT")
    void getUsuarios_claimAdministradorPeroRolParticipante_retorna403() throws Exception {
        usuarioExiste("part@test.x", "PARTICIPANTE");

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenPara("part@test.x", "ADMINISTRADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/usuarios con token revocado -> 401")
    void getUsuarios_conTokenRevocado_retorna401() throws Exception {
        usuarioExiste("admin@test.x", "ADMINISTRADOR");
        String token = tokenPara("admin@test.x", "ADMINISTRADOR");
        when(tokenRevocationService.estaRevocado(token)).thenReturn(true);

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/usuarios con usuario inactivo -> 401")
    void getUsuarios_conUsuarioInactivo_retorna401() throws Exception {
        usuarioExiste("inactivo@test.x", "ADMINISTRADOR", false);

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenPara("inactivo@test.x", "ADMINISTRADOR")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/eventos con token PARTICIPANTE -> 403 (requiere ORGANIZADOR)")
    void postEventos_conParticipante_retorna403() throws Exception {
        usuarioExiste("part@test.x", "PARTICIPANTE");

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Authorization", "Bearer " + tokenPara("part@test.x", "PARTICIPANTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/eventos con token ORGANIZADOR y body valido -> 201")
    void postEventos_conOrganizadorBodyValido_retorna201() throws Exception {
        usuarioExiste("org@test.x", "ORGANIZADOR");
        when(eventoService.crear(any(EventoRequest.class), anyString()))
                .thenReturn(eventoResponse());

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Authorization", "Bearer " + tokenPara("org@test.x", "ORGANIZADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/eventos con token ORGANIZADOR y body invalido -> 400 (validacion)")
    void postEventos_conOrganizadorBodyInvalido_retorna400() throws Exception {
        usuarioExiste("org@test.x", "ORGANIZADOR");

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Authorization", "Bearer " + tokenPara("org@test.x", "ORGANIZADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_INVALIDO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/usuarios con PARTICIPANTE -> 403 (sin users:read)")
    void getUsuarios_conParticipante_retorna403() throws Exception {
        usuarioExiste("part@test.x", "PARTICIPANTE");

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenPara("part@test.x", "PARTICIPANTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/usuarios con ADMINISTRADOR y users:read -> 200")
    void getUsuarios_conAdministrador_retorna200() throws Exception {
        usuarioExiste("admin@test.x", "ADMINISTRADOR");
        when(usuarioService.listar(null, null, 1, 20))
                .thenReturn(new UsuarioPageResponse(List.of(), 1, 20, 0, 0));

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenPara("admin@test.x", "ADMINISTRADOR")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/ubicaciones exige autenticación y permiso de lectura")
    void getUbicaciones_conOrganizador_retorna200() throws Exception {
        usuarioExiste("org@test.x", "ORGANIZADOR");
        when(ubicacionService.listar("Virtual", 1, 20))
                .thenReturn(new PageResponse<>(List.of(), 1, 20, 0, 0));

        mockMvc.perform(get("/api/v1/ubicaciones")
                        .param("tipo", "Virtual")
                        .header("Authorization", "Bearer " + tokenPara("org@test.x", "ORGANIZADOR")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/recursos admite lectura para ORGANIZADOR")
    void getRecursos_conOrganizador_retorna200() throws Exception {
        usuarioExiste("org@test.x", "ORGANIZADOR");
        when(recursoService.listar(1, 20))
                .thenReturn(new PageResponse<>(List.of(), 1, 20, 0, 0));

        mockMvc.perform(get("/api/v1/recursos")
                        .header("Authorization", "Bearer " + tokenPara("org@test.x", "ORGANIZADOR")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/ubicaciones/{id} queda reservado a ADMINISTRADOR")
    void deleteUbicaciones_conOrganizador_retorna403() throws Exception {
        usuarioExiste("org@test.x", "ORGANIZADOR");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/ubicaciones/1")
                        .header("Authorization", "Bearer " + tokenPara("org@test.x", "ORGANIZADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/recursos con PARTICIPANTE -> 403")
    void getRecursos_conParticipante_retorna403() throws Exception {
        usuarioExiste("part@test.x", "PARTICIPANTE");

        mockMvc.perform(get("/api/v1/recursos")
                        .header("Authorization", "Bearer " + tokenPara("part@test.x", "PARTICIPANTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/ubicaciones/{id} permite ADMINISTRADOR")
    void deleteUbicaciones_conAdministrador_retorna204() throws Exception {
        usuarioExiste("admin@test.x", "ADMINISTRADOR");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/ubicaciones/1")
                        .header("Authorization", "Bearer " + tokenPara("admin@test.x", "ADMINISTRADOR")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/recursos/{id} permite ADMINISTRADOR")
    void deleteRecursos_conAdministrador_retorna204() throws Exception {
        usuarioExiste("admin@test.x", "ADMINISTRADOR");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/recursos/1")
                        .header("Authorization", "Bearer " + tokenPara("admin@test.x", "ADMINISTRADOR")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/eventos/mis-eventos con token ORGANIZADOR -> 200")
    void getMisEventos_conOrganizador_retorna200() throws Exception {
        usuarioExiste("org@test.x", "ORGANIZADOR");
        when(eventoService.misEventos(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/eventos/mis-eventos")
                        .header("Authorization", "Bearer " + tokenPara("org@test.x", "ORGANIZADOR")))
                .andExpect(status().isOk());
    }
}
