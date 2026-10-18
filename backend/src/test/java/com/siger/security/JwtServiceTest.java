package com.siger.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios de {@link JwtService}: generacion y validacion de tokens JWT.
 *
 * <p>No carga contexto de Spring (es un test puro). Como {@code JwtService}
 * usa inyeccion por {@code @Value} y un {@code @PostConstruct}, se instancia
 * con {@code new}, se inyectan los campos privados con
 * {@link ReflectionTestUtils#setField} y se invoca {@code init()} a mano (es
 * package-private y este test esta en el mismo paquete).
 *
 * <p>Cubre: generacion de token valido, lectura de claims (subject + rol),
 * token expirado, firma alterada, token firmado con otro secreto, doble
 * generacion del mismo usuario, y rechazo de secreto con menos de 32 bytes.
 */
class JwtServiceTest {

    private static final String SECRET = "clave-secreta-de-prueba-de-al-menos-32-caracteres-siger";
    private static final long EXPIRACION_MS = 3_600_000L; // 1 hora

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRACION_MS);
        jwtService.init(); // @PostConstruct, package-private: accesible desde este paquete
    }

    // ------------------------------------------------------------------
    //  Generacion
    // ------------------------------------------------------------------

    @Test
    @DisplayName("generar: retorna un token no nulo con estructura JWT (3 partes separadas por '.')")
    void generar_retornaTokenConEstructuraJWT() {
        String token = jwtService.generar("user@test.x", "PARTICIPANTE");

        assertThat(token).isNotNull();
        assertThat(token.split("\\."))
                .as("un JWT tiene header.payload.signature (3 partes)")
                .hasSize(3);
    }

    @Test
    @DisplayName("generar: dos tokens para el mismo usuario decodifican al mismo subject y rol")
    void generar_dosTokensParaMismoUsuario_decodificanAlMismoSubjectYRol() {
        String t1 = jwtService.generar("user@test.x", "PARTICIPANTE");
        String t2 = jwtService.generar("user@test.x", "PARTICIPANTE");

        Claims c1 = jwtService.leer(t1);
        Claims c2 = jwtService.leer(t2);

        assertThat(c1.getSubject()).isEqualTo("user@test.x");
        assertThat(c2.getSubject()).isEqualTo("user@test.x");
        assertThat(c1.get("rol")).isEqualTo("PARTICIPANTE");
        assertThat(c2.get("rol")).isEqualTo("PARTICIPANTE");
    }

    // ------------------------------------------------------------------
    //  Lectura valida
    // ------------------------------------------------------------------

    @Test
    @DisplayName("leer: token recien generado -> subject y rol correctos")
    void leer_tokenRecienGenerado_retornaSubjectYRol() {
        String token = jwtService.generar("admin@siger.test", "ADMINISTRADOR");

        Claims claims = jwtService.leer(token);

        assertThat(claims.getSubject()).as("subject (email)").isEqualTo("admin@siger.test");
        assertThat(claims.get("rol")).as("claim rol").isEqualTo("ADMINISTRADOR");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    // ------------------------------------------------------------------
    //  Casos de fallo
    // ------------------------------------------------------------------

    @Test
    @DisplayName("leer: token expirado -> ExpiredJwtException")
    void leer_tokenExpirado_lanzaExpiredJwt() throws InterruptedException {
        JwtService efimero = newServicioConExpiracion(1L);
        String token = efimero.generar("part@test.x", "PARTICIPANTE");

        Thread.sleep(50); // dejar que el token (exp 1 ms) caduque

        assertThatThrownBy(() -> jwtService.leer(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("leer: token con la firma modificada -> JwtException")
    void leer_tokenConFirmaModificada_lanzaJwtException() {
        String token = jwtService.generar("org@test.x", "ORGANIZADOR");
        String[] partes = token.split("\\.");
        // Alterar el ultimo segmento (la firma)
        String tokenAlterado = partes[0] + "." + partes[1] + "." + partes[2] + "x";

        assertThatThrownBy(() -> jwtService.leer(tokenAlterado))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("leer: token firmado con otro secreto -> JwtException")
    void leer_tokenFirmadoConOtroSecreto_lanzaJwtException() {
        JwtService otro = newServicioConExpiracion(EXPIRACION_MS);
        ReflectionTestUtils.setField(otro, "secret",
                "otro-secreto-de-al-menos-32-bytes-distinto-del-original");
        otro.init();
        String tokenExterno = otro.generar("org@test.x", "ORGANIZADOR");

        assertThatThrownBy(() -> jwtService.leer(tokenExterno))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("leer: cadena que no es un JWT -> JwtException")
    void leer_cadenaNoJwt_lanzaJwtException() {
        assertThatThrownBy(() -> jwtService.leer("esto-no-es-un-jwt"))
                .isInstanceOf(JwtException.class);
    }

    // ------------------------------------------------------------------
    //  Validacion del secreto en init()
    // ------------------------------------------------------------------

    @Test
    @DisplayName("init: secreto con menos de 32 bytes -> IllegalStateException")
    void init_secretoCorto_lanzaIllegalState() {
        JwtService conSecretoCorto = new JwtService();
        ReflectionTestUtils.setField(conSecretoCorto, "secret", "corto"); // 5 bytes
        ReflectionTestUtils.setField(conSecretoCorto, "expirationMs", EXPIRACION_MS);

        assertThatThrownBy(conSecretoCorto::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    // ------------------------------------------------------------------
    //  Helper
    // ------------------------------------------------------------------

    private JwtService newServicioConExpiracion(long expiracionMs) {
        JwtService svc = new JwtService();
        ReflectionTestUtils.setField(svc, "secret", SECRET);
        ReflectionTestUtils.setField(svc, "expirationMs", expiracionMs);
        svc.init();
        return svc;
    }
}
