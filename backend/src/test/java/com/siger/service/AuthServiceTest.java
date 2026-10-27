package com.siger.service;

import com.siger.dto.auth.AuthResponse;
import com.siger.dto.auth.RegistroRequest;
import com.siger.exception.BusinessException;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.ParticipanteRepository;
import com.siger.repository.UsuarioRepository;
import com.siger.security.JwtService;
import com.siger.security.TokenRevocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock ParticipanteRepository participanteRepository;
    @Mock OrganizadorRepository organizadorRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock TokenRevocationService tokenRevocationService;
    @InjectMocks AuthService authService;

    @Test
    void registro_creaParticipanteYGeneraTokenConSuRol() {
        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(participanteRepository.save(any(Participante.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generar("ana@example.com", "PARTICIPANTE")).thenReturn("token");

        AuthResponse result = authService.registro(registro("PARTICIPANTE"));

        assertThat(result.rol()).isEqualTo("PARTICIPANTE");
        assertThat(result.token()).isEqualTo("token");
        verify(participanteRepository).save(any(Participante.class));
        verify(organizadorRepository, never()).save(any(Organizador.class));
    }

    @Test
    void registro_creaOrganizadorYGeneraTokenConSuRol() {
        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(organizadorRepository.save(any(Organizador.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generar("ana@example.com", "ORGANIZADOR")).thenReturn("token");

        AuthResponse result = authService.registro(registro("organizador"));

        assertThat(result.rol()).isEqualTo("ORGANIZADOR");
        assertThat(result.token()).isEqualTo("token");
        verify(organizadorRepository).save(any(Organizador.class));
        verify(participanteRepository, never()).save(any(Participante.class));
    }

    @Test
    void registro_rechazaRolesNoPermitidos() {
        assertThatThrownBy(() -> authService.registro(registro("ADMINISTRADOR")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("PARTICIPANTE u ORGANIZADOR");

        verify(participanteRepository, never()).save(any(Participante.class));
        verify(organizadorRepository, never()).save(any(Organizador.class));
    }

    private RegistroRequest registro(String rol) {
        return new RegistroRequest(
            "Ana", "Pérez", "ana@example.com", "70000000", "+503", "password123", rol
        );
    }
}
