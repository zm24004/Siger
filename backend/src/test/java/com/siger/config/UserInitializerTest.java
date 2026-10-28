package com.siger.config;

import com.siger.model.Administrador;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Usuario;
import com.siger.repository.AdministradorRepository;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.ParticipanteRepository;
import com.siger.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInitializerTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock AdministradorRepository administradorRepository;
    @Mock OrganizadorRepository organizadorRepository;
    @Mock ParticipanteRepository participanteRepository;
    @Mock PasswordEncoder passwordEncoder;

    private final Map<String, Usuario> usuarios = new HashMap<>();
    private UserInitializer initializer;

    @BeforeEach
    void setUp() {
        when(usuarioRepository.findByEmail(anyString()))
            .thenAnswer(invocation -> Optional.ofNullable(usuarios.get(invocation.getArgument(0))));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuarios.put(usuario.getEmail(), usuario);
            return usuario;
        });
        when(administradorRepository.save(any(Administrador.class))).thenAnswer(invocation -> {
            Administrador usuario = invocation.getArgument(0);
            usuarios.put(usuario.getEmail(), usuario);
            return usuario;
        });
        when(organizadorRepository.save(any(Organizador.class))).thenAnswer(invocation -> {
            Organizador usuario = invocation.getArgument(0);
            usuarios.put(usuario.getEmail(), usuario);
            return usuario;
        });
        when(participanteRepository.save(any(Participante.class))).thenAnswer(invocation -> {
            Participante usuario = invocation.getArgument(0);
            usuarios.put(usuario.getEmail(), usuario);
            return usuario;
        });
        when(passwordEncoder.encode(anyString()))
            .thenAnswer(invocation -> "encoded:" + invocation.getArgument(0));

        initializer = new UserInitializer(usuarioRepository, administradorRepository,
            organizadorRepository, participanteRepository, passwordEncoder);
    }

    @Test
    void creaYActualizaElAdministradorYCreaLosUsuariosDemoUnaSolaVez() {
        initializer.run(new String[0]);
        initializer.run(new String[0]);

        Usuario administrador = usuarios.get("admin@siger.com");
        assertThat(administrador).isInstanceOf(Administrador.class);
        assertThat(administrador.getNombre()).isEqualTo("Administrador");
        assertThat(administrador.getApellido()).isEqualTo("General");
        assertThat(administrador.getContrasena()).isEqualTo("encoded:Admin123");
        assertThat(administrador.getEstado()).isTrue();

        Usuario organizador = usuarios.get("organizador@siger.com");
        assertThat(organizador).isInstanceOf(Organizador.class);
        assertThat(organizador.getNombre()).isEqualTo("Organizador");
        assertThat(organizador.getApellido()).isEqualTo("Demo");
        assertThat(organizador.getTelefono()).isEmpty();
        assertThat(organizador.getCodigoPais()).isEmpty();
        assertThat(organizador.getEstado()).isTrue();
        assertThat(organizador.getContrasena()).isEqualTo("encoded:Organizador123");

        Usuario participanteUno = usuarios.get("participante1@siger.com");
        assertThat(participanteUno).isInstanceOf(Participante.class);
        assertThat(participanteUno.getNombre()).isEqualTo("Participante");
        assertThat(participanteUno.getApellido()).isEqualTo("Uno");
        assertThat(participanteUno.getContrasena()).isEqualTo("encoded:Participante123");

        Usuario participanteDos = usuarios.get("participante2@siger.com");
        assertThat(participanteDos).isInstanceOf(Participante.class);
        assertThat(participanteDos.getApellido()).isEqualTo("Dos");
        assertThat(participanteDos.getContrasena()).isEqualTo("encoded:Participante123");

        verify(administradorRepository, times(1)).save(any(Administrador.class));
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(organizadorRepository, times(1)).save(any(Organizador.class));
        verify(participanteRepository, times(2)).save(any(Participante.class));
    }
}
