package com.siger.config;

import com.siger.model.Administrador;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Usuario;
import com.siger.repository.AdministradorRepository;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.ParticipanteRepository;
import com.siger.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminUserInitializer.class);

    private static final String ADMIN_EMAIL = "admin@siger.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_NOMBRE = "Administrador";
    private static final String ADMIN_APELLIDO = "General";
    private static final String ORGANIZADOR_EMAIL = "organizador@siger.com";
    private static final String PARTICIPANTE_UNO_EMAIL = "participante1@siger.com";
    private static final String PARTICIPANTE_DOS_EMAIL = "participante2@siger.com";
    private static final String ORGANIZADOR_PASSWORD = "organizador123";
    private static final String PARTICIPANTE_PASSWORD = "participante123";

    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;
    private final OrganizadorRepository organizadorRepository;
    private final ParticipanteRepository participanteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        usuarioRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(
            this::actualizarAdministrador,
            this::crearAdministrador
        );
        crearUsuarioDemo(ORGANIZADOR_EMAIL, "Organizador", "Demo", Organizador.class,
            Organizador::new, organizadorRepository::save, ORGANIZADOR_PASSWORD);
        crearUsuarioDemo(PARTICIPANTE_UNO_EMAIL, "Participante", "Uno", Participante.class,
            Participante::new, participanteRepository::save, PARTICIPANTE_PASSWORD);
        crearUsuarioDemo(PARTICIPANTE_DOS_EMAIL, "Participante", "Dos", Participante.class,
            Participante::new, participanteRepository::save, PARTICIPANTE_PASSWORD);
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

    private <T extends Usuario> void crearUsuarioDemo(
        String email,
        String nombre,
        String apellido,
        Class<T> tipo,
        Supplier<T> crear,
        Consumer<T> guardar,
        String contrasena
    ) {
        Usuario existente = usuarioRepository.findByEmail(email).orElse(null);
        if (existente != null) {
            if (!tipo.isInstance(existente)) {
                LOG.warn("No se creo {}: el email ya pertenece al rol {}",
                    email, existente.getClass().getSimpleName());
            } else {
                LOG.info("Usuario demo {} ya existe y se mantiene sin cambios: {}",
                    tipo.getSimpleName(), email);
            }
            return;
        }

        T usuario = crear.get();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setTelefono("");
        usuario.setCodigoPais("");
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setEstado(true);
        guardar.accept(usuario);
        LOG.info("Usuario demo {} creado: {}", tipo.getSimpleName(), email);
    }
}
