package com.siger.security;

import com.siger.model.Administrador;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Usuario;
import com.siger.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads a Usuario by email and builds a Spring Security UserDetails with
 * a single authority derived from the concrete subtype (Administrador /
 * Organizador / Participante).
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        if (Boolean.FALSE.equals(u.getEstado())) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        String rol;
        if (u instanceof Administrador) {
            rol = "ADMINISTRADOR";
        } else if (u instanceof Organizador) {
            rol = "ORGANIZADOR";
        } else if (u instanceof Participante) {
            rol = "PARTICIPANTE";
        } else {
            throw new IllegalStateException("Tipo de usuario no soportado: " + u.getClass().getName());
        }

        var authority = new SimpleGrantedAuthority("ROLE_" + rol);
        return User.builder()
            .username(u.getEmail())
            .password(u.getContrasena())
            .disabled(false)
            .accountExpired(false)
            .credentialsExpired(false)
            .accountLocked(false)
            .authorities(List.of(authority))
            .build();
    }
}
