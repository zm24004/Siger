package com.siger.security;

import com.siger.config.SecurityConfig;
import com.siger.controller.EventoController;
import com.siger.controller.UsuarioController;
import com.siger.dto.EventoRequest;
import com.siger.dto.EventoResponse;
import com.siger.model.Administrador;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Usuario;
import com.siger.repository.UsuarioRepository;
import com.siger.service.EventoService;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la configuracion de seguridad ({@link SecurityConfig}) usando
 * MockMvc con el slice {@code @WebMvcTest}.
 *
 * <p>Se cargan los controladores reales ({@link EventoController} y
 * {@link UsuarioController}) junto con la cadena de filtros de Spring Security
 * ({@code SecurityFilterChain}, {@link JwtFilter}, {@link JwtService},
 * {@link UserDetailsServiceImpl} y los handlers 401/403). Los servicios y
 * {@link UsuarioRepository} se reemplazan por mocks de Mockito.
 *
 * <p>El secreto JWT y el origen CORS provienen de
 * {@code application-test.properties} (perfil "test"); no se usa base de datos.
 *
 * <p>Cubre: rutas publicas (GET /api/eventos, GET /api/eventos/{id}), rutas
 * protegidas sin token (401), token invalido (401), control de roles con
 * {@code @PreAuthorize} (PARTICIPANTE 403, ORGANIZADOR 200, ADMINISTRADOR 200)
 * y validacion de Bean Validation (400) tras pasar la autorizacion.
 */
@WebMvcTest(controllers = {EventoController.class, UsuarioController.class})
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
    @MockitoBean UsuarioRepository usuarioRepository;

    // ------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------

    private String tokenPara(String email, String rol) {
        return jwtService.generar(email, rol);
    }

    /** Hace que UserDetailsServiceImpl encuentre al usuario por email. */
    private void usuarioExiste(String email, String tipo) {
        Usuario user = switch (tipo) {
            case "ADMINISTRADOR" -> new Administrador();
            case "ORGANIZADOR" -> new Organizador();
            default -> new Participante();
        };
        user.setEmail(email);
        user.setNombre("User " + email);
        user.setContrasena("hash-bcrypt");
        user.setEstado(true);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(user));
    }

    private EventoResponse eventoResponse() {
        return new EventoResponse(
                1L, "Evento test", "desc", LocalDate.of(2099, 1, 1),
                LocalTime.of(9, 0), LocalTime.of(11, 0), 50, "Activo",
                1L, "Auditorio", 1L, "Org", 50);
    }

    /** Body de POST /api/eventos valido (pasa @Valid). */
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

    /** Body de POST /api/eventos invalido (violaciones de @NotBlank/@NotNull/@Positive). */
    private static final String BODY_INVALIDO = """
            {"descripcion":"sin nombre","capacidadMaxima":0,"idUbicacion":1}
            """;

    // ------------------------------------------------------------------
    //  Rutas publicas (permitAll)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/eventos sin token -> 200 (permitAll)")
    void getEventos_sinToken_retorna200() throws Exception {
        when(eventoService.catalogoActivos()).thenReturn(List.of());

        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/eventos/{id} sin token -> 200 (permitAll)")
    void getEventoPorId_sinToken_retorna200() throws Exception {
        when(eventoService.detalle(1L)).thenReturn(eventoResponse());

        mockMvc.perform(get("/api/eventos/1"))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------
    //  Rutas protegidas sin token -> 401
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/eventos sin token -> 401 (AuthEntryPointJwt)")
    void postEventos_sinToken_retorna401() throws Exception {
        mockMvc.perform(post("/api/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/usuarios sin token -> 401 (requiere ADMINISTRADOR)")
    void getUsuarios_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    //  Token invalido -> 401
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/eventos con token invalido -> 401")
    void postEventos_conTokenInvalido_retorna401() throws Exception {
        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer cadena-que-no-es-un-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    //  Control de roles (@PreAuthorize)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/eventos con token PARTICIPANTE -> 403 (requiere ORGANIZADOR)")
    void postEventos_conParticipante_retorna403() throws Exception {
        usuarioExiste("part@test.x", "PARTICIPANTE");

        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + tokenPara("part@test.x", "PARTICIPANTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/eventos con token ORGANIZADOR y body valido -> 200")
    void postEventos_conOrganizadorBodyValido_retorna200() throws Exception {
        usuarioExiste("org@test.x", "ORGANIZADOR");
        when(eventoService.crear(any(EventoRequest.class), anyString()))
                .thenReturn(eventoResponse());

        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + tokenPara("org@test.x", "ORGANIZADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/eventos con token ORGANIZADOR y body invalido -> 400 (validacion)")
    void postEventos_conOrganizadorBodyInvalido_retorna400() throws Exception {
        usuarioExiste("org@test.x", "ORGANIZADOR");

        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + tokenPara("org@test.x", "ORGANIZADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_INVALIDO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/usuarios con token PARTICIPANTE -> 403 (requiere ADMINISTRADOR)")
    void getUsuarios_conParticipante_retorna403() throws Exception {
        usuarioExiste("part@test.x", "PARTICIPANTE");
        when(usuarioService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenPara("part@test.x", "PARTICIPANTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/usuarios con token ADMINISTRADOR -> 200")
    void getUsuarios_conAdministrador_retorna200() throws Exception {
        usuarioExiste("admin@test.x", "ADMINISTRADOR");
        when(usuarioService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenPara("admin@test.x", "ADMINISTRADOR")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/eventos/mis-eventos con token ORGANIZADOR -> 200")
    void getMisEventos_conOrganizador_retorna200() throws Exception {
        usuarioExiste("org@test.x", "ORGANIZADOR");
        when(eventoService.misEventos(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/eventos/mis-eventos")
                        .header("Authorization", "Bearer " + tokenPara("org@test.x", "ORGANIZADOR")))
                .andExpect(status().isOk());
    }
}
