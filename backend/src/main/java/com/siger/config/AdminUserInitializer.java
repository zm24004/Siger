package com.siger.config;

import com.siger.model.Administrador;
import com.siger.model.Usuario;
import com.siger.repository.AdministradorRepository;
import com.siger.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminUserInitializer.class);

    private static final String ADMIN_EMAIL = "admin@siger.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_NOMBRE = "Administrador";
    private static final String ADMIN_APELLIDO = "General";

    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        usuarioRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(
            this::actualizarAdministrador,
            this::crearAdministrador
        );
    }

    private void crearAdministrador() {
        Administrador admin = new Administrador();
        admin.setNombre(ADMIN_NOMBRE);
        admin.setApellido(ADMIN_APELLIDO);
        admin.setEmail(ADMIN_EMAIL);
        admin.setContrasena(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setEstado(true);
        administradorRepository.save(admin);
        LOG.info("Usuario administrador general creado: {}", ADMIN_EMAIL);
    }

    private void actualizarAdministrador(Usuario usuario) {
        if (!(usuario instanceof Administrador)) {
            LOG.warn("No se actualizo {}: el email ya pertenece al rol {}",
                ADMIN_EMAIL, usuario.getClass().getSimpleName());
            return;
        }
        usuario.setNombre(ADMIN_NOMBRE);
        usuario.setApellido(ADMIN_APELLIDO);
        usuario.setContrasena(passwordEncoder.encode(ADMIN_PASSWORD));
        usuario.setEstado(true);
        usuarioRepository.save(usuario);
        LOG.info("Usuario administrador general actualizado: {}", ADMIN_EMAIL);
    }
}
