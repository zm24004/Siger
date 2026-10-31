package com.siger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.siger.security.AccessDeniedHandlerJwt;
import com.siger.security.AuthEntryPointJwt;
import com.siger.security.JwtFilter;

import java.util.List;

/**
 * Spring Security configuration: stateless JWT, CORS, route authorization.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AuthEntryPointJwt authEntryPointJwt;
    private final AccessDeniedHandlerJwt accessDeniedHandlerJwt;

    public SecurityConfig(JwtFilter jwtFilter,
                          AuthEntryPointJwt authEntryPointJwt,
                          AccessDeniedHandlerJwt accessDeniedHandlerJwt) {
        this.jwtFilter = jwtFilter;
        this.authEntryPointJwt = authEntryPointJwt;
        this.accessDeniedHandlerJwt = accessDeniedHandlerJwt;
    }

    @Value("${siger.cors.origin}")
    private String corsOrigin;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public authentication endpoints
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/api/v1/auth/forgot-password",
                    "/api/v1/public/**"
                ).permitAll()
                // WebSocket / STOMP handshake endpoint — the actual authentication
                // happens at the STOMP CONNECT frame via JwtChannelInterceptor,
                // so the HTTP handshake (and SockJS info/XHR) must be public.
                .requestMatchers("/api/v1/ws-notificaciones/**").permitAll()
                // User management is admin-only
                .requestMatchers("/api/v1/usuarios/**", "/api/v1/roles/**").authenticated()
                // OpenAPI / Swagger
                .requestMatchers(
                    "/openapi.yml",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Actuator health/info (no secrets)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated())
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(authEntryPointJwt)
                .accessDeniedHandler(accessDeniedHandlerJwt))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(corsOrigin));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
