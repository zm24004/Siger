package com.siger.security;

import com.siger.model.Administrador;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Usuario;
import com.siger.model.Rol;
import com.siger.repository.UsuarioRepository;
import com.siger.repository.RolRepository;
import com.siger.service.RolHelper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Loads a Usuario by email and builds a Spring Security UserDetails with
 * a single authority derived from the concrete subtype (Administrador /
 * Organizador / Participante).
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        Usuario u = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        if (Boolean.FALSE.equals(u.getEstado())) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        String codigoRol = RolHelper.codigoRol(u);
        Rol rol = rolRepository.findById(codigoRol)
            .orElseThrow(() -> new IllegalStateException("No existe la configuración del rol " + codigoRol));
        var authorities = new ArrayList<SimpleGrantedAuthority>();
        rol.getPermisos().forEach(permiso ->
            authorities.add(new SimpleGrantedAuthority(permiso.getIdPermiso())));
        return User.builder()
            .username(u.getEmail())
            .password(u.getContrasena())
            .disabled(false)
            .accountExpired(false)
            .credentialsExpired(false)
            .accountLocked(false)
            .authorities(authorities)
            .build();
    }
}
