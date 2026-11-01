package com.siger.service;

import com.siger.dto.UsuarioCreateRequest;
import com.siger.dto.UsuarioEstadoRequest;
import com.siger.dto.UsuarioPageResponse;
import com.siger.dto.UsuarioResponse;
import com.siger.dto.UsuarioUpdateRequest;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Administrador;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Usuario;
import com.siger.repository.UsuarioRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UsuarioService usuarioService;

    @Test
    void listar_aplicaFiltrosYConvierteLosResultados() {
        Participante participante = participante(12L, "ana@example.com");
        PageImpl<Usuario> page = new PageImpl<>(List.of(participante), PageRequest.of(1, 5), 6);
        when(usuarioRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        UsuarioPageResponse result = usuarioService.listar("PAR", true, 2, 5);

        assertThat(result.page()).isEqualTo(2);
        assertThat(result.limit()).isEqualTo(5);
        assertThat(result.totalElements()).isEqualTo(6);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.content()).singleElement()
            .satisfies(response -> {
                assertThat(response.idUsuario()).isEqualTo(12L);
                assertThat(response.rol()).isEqualTo("PARTICIPANTE");
            });

        ArgumentCaptor<Specification<Usuario>> specificationCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(usuarioRepository).findAll(specificationCaptor.capture(), eq(PageRequest.of(1, 5)));
        assertThat(specificationCaptor.getValue()).isNotNull();
    }

    @Test
    void listar_ajustaUnaPaginaNoPositivaAlIndiceInicial() {
        when(usuarioRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of()));

        UsuarioPageResponse result = usuarioService.listar(null, null, 0, 10);

        assertThat(result.page()).isZero();
        verify(usuarioRepository).findAll(any(Specification.class), eq(PageRequest.of(0, 10)));
    }

    @Test
    void listar_rechazaUnRolInvalidoSinConsultarElRepositorio() {
        assertThatThrownBy(() -> usuarioService.listar("invitado", null, 1, 10))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Rol inválido");

        verify(usuarioRepository, never()).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void listar_construyePredicadosParaEstadoYTipoDeUsuario() {
        when(usuarioRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of()));
        usuarioService.listar("PAR", true, 1, 10);
        ArgumentCaptor<Specification<Usuario>> specificationCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(usuarioRepository).findAll(specificationCaptor.capture(), any(PageRequest.class));

        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<?> criteriaQuery = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<Usuario> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        Path<Object> estadoPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<Class<? extends Usuario>> typeExpression = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(predicate);
        when(criteriaBuilder.equal(any(Expression.class), any(Object.class))).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        when(root.get("estado")).thenReturn(estadoPath);
        when(root.type()).thenReturn(typeExpression);

        specificationCaptor.getValue().toPredicate(root, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder).equal(estadoPath, true);
        verify(criteriaBuilder).equal(typeExpression, Participante.class);
        verify(criteriaBuilder, times(2)).and(predicate, predicate);
    }

    @Test
    void detalle_devuelveElUsuarioEncontrado() {
        Usuario usuario = organizador(7L, "org@example.com");
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));

        UsuarioResponse result = usuarioService.detalle(7L);

        assertThat(result.idUsuario()).isEqualTo(7L);
        assertThat(result.rol()).isEqualTo("ORGANIZADOR");
    }

    @Test
    void detalle_lanzaNotFoundCuandoNoExisteElUsuario() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.detalle(404L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("404");
    }

    @Test
    void perfil_buscaPorCorreoYLanzaNotFoundSiNoExiste() {
        when(usuarioRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.perfil("ana@example.com"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("autenticado");
    }

    @Test
    void crear_normalizaDatosCodigosDeRolYEncriptaLaContrasena() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse response = usuarioService.crear(
            new UsuarioCreateRequest(" Ana ", " Pérez ", " ANA@EXAMPLE.COM ", " 70000000 ", " +503 ",
                "password123", " participante "));

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario saved = usuarioCaptor.getValue();
        assertThat(saved).isInstanceOf(Participante.class);
        assertThat(saved.getNombre()).isEqualTo("Ana");
        assertThat(saved.getApellido()).isEqualTo("Pérez");
        assertThat(saved.getEmail()).isEqualTo("ana@example.com");
        assertThat(saved.getTelefono()).isEqualTo("70000000");
        assertThat(saved.getCodigoPais()).isEqualTo("+503");
        assertThat(saved.getContrasena()).isEqualTo("hash");
        assertThat(saved.getEstado()).isTrue();
        assertThat(response.rol()).isEqualTo("PARTICIPANTE");
        verify(usuarioRepository).existsByEmail("ana@example.com");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void crear_admiteLosTresCodigosDeRol() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse administrador = usuarioService.crear(createRequest("ADM", "adm@example.com"));
        UsuarioResponse organizador = usuarioService.crear(createRequest("ORG", "org@example.com"));
        UsuarioResponse participante = usuarioService.crear(createRequest("PAR", "par@example.com"));

        assertThat(administrador.rol()).isEqualTo("ADMINISTRADOR");
        assertThat(organizador.rol()).isEqualTo("ORGANIZADOR");
        assertThat(participante.rol()).isEqualTo("PARTICIPANTE");
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, org.mockito.Mockito.times(3)).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getAllValues())
            .extracting(usuario -> usuario.getClass().getName())
            .containsExactly(
                Administrador.class.getName(),
                Organizador.class.getName(),
                Participante.class.getName());
    }

    @Test
    void crear_rechazaCorreoDuplicadoAntesDeEncriptarOGuardar() {
        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crear(createRequest("PAR", "ana@example.com")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Ya existe");

        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crear_detectaDuplicadosDespuesDeNormalizarElCorreo() {
        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crear(new UsuarioCreateRequest(
            "Ana", "Pérez", " ANA@EXAMPLE.COM ", "70000000", "+503", "password123", "PAR")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Ya existe");

        verify(usuarioRepository).existsByEmail("ana@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crear_rechazaRolDesconocido() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.crear(createRequest("INV", "ana@example.com")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Rol inválido");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizar_normalizaDatosYGuardaElUsuario() {
        Usuario usuario = participante(12L, "old@example.com");
        when(usuarioRepository.findById(12L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailAndIdUsuarioNot("new@example.com", 12L)).thenReturn(false);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponse result = usuarioService.actualizar(12L, new UsuarioUpdateRequest(
            " Ana ", " Pérez ", " NEW@EXAMPLE.COM ", " 70000000 ", " +503 "));

        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(usuario.getNombre()).isEqualTo("Ana");
        assertThat(usuario.getApellido()).isEqualTo("Pérez");
        assertThat(usuario.getTelefono()).isEqualTo("70000000");
        assertThat(usuario.getCodigoPais()).isEqualTo("+503");
        verify(usuarioRepository).existsByEmailAndIdUsuarioNot("new@example.com", 12L);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void actualizar_rechazaCorreoUsadoPorOtroUsuario() {
        Usuario usuario = participante(12L, "old@example.com");
        when(usuarioRepository.findById(12L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailAndIdUsuarioNot("taken@example.com", 12L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.actualizar(12L, new UsuarioUpdateRequest(
            "Ana", "Pérez", "taken@example.com", "70000000", "+503")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Ya existe");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cambiarEstado_actualizaYGuardaElEstado() {
        Usuario usuario = participante(12L, "ana@example.com");
        when(usuarioRepository.findById(12L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponse result = usuarioService.cambiarEstado(12L, new UsuarioEstadoRequest(false));

        assertThat(result.estado()).isFalse();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void eliminar_desactivaSinBorrarFisicamenteElUsuario() {
        Usuario usuario = participante(12L, "ana@example.com");
        when(usuarioRepository.findById(12L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        usuarioService.eliminar(12L);

        assertThat(usuario.getEstado()).isFalse();
        verify(usuarioRepository).save(usuario);
    }

    private static UsuarioCreateRequest createRequest(String rol, String email) {
        return new UsuarioCreateRequest("Ana", "Pérez", email, "70000000", "+503", "password123", rol);
    }

    private static Participante participante(Long id, String email) {
        Participante participante = new Participante();
        participante.setIdUsuario(id);
        participante.setNombre("Ana");
        participante.setApellido("Pérez");
        participante.setEmail(email);
        participante.setTelefono("70000000");
        participante.setCodigoPais("+503");
        participante.setEstado(true);
        participante.setFechaRegistro(LocalDateTime.of(2026, 1, 1, 12, 0));
        return participante;
    }

    private static Organizador organizador(Long id, String email) {
        Organizador organizador = new Organizador();
        organizador.setIdUsuario(id);
        organizador.setNombre("Organizador");
        organizador.setApellido("Ejemplo");
        organizador.setEmail(email);
        organizador.setTelefono("70000000");
        organizador.setCodigoPais("+503");
        organizador.setEstado(true);
        return organizador;
    }
}
