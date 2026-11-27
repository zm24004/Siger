package com.siger.service;

import com.siger.dto.NotificacionCursor;
import com.siger.dto.NotificacionEnvioRequest;
import com.siger.exception.NotFoundException;
import com.siger.model.Evento;
import com.siger.model.Notificacion;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Reservacion;
import com.siger.repository.EventoRepository;
import com.siger.repository.NotificacionRepository;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.ReservacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock NotificacionRepository notificacionRepository;
    @Mock EventoRepository eventoRepository;
    @Mock OrganizadorRepository organizadorRepository;
    @Mock ReservacionRepository reservacionRepository;
    @Mock NotificacionStompService notificacionStompService;

    @InjectMocks NotificacionService service;

    @Test
    void listarPaginaPorUsuario_solicitaUnaFilaExtraYConstruyeCursorSiguiente() {
        LocalDateTime fechaNueva = LocalDateTime.of(2026, 11, 1, 12, 0);
        LocalDateTime fechaAntigua = fechaNueva.minusMinutes(1);
        Notificacion primera = crearNotificacion(20L, fechaNueva);
        Notificacion segunda = crearNotificacion(19L, fechaAntigua);

        when(notificacionRepository.buscarPagina(eq(7L), isNull(), isNull(), any()))
            .thenReturn(List.of(primera, segunda));

        var response = service.listarPaginaPorUsuario(7L, null, 1);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().idNotificacion()).isEqualTo(20L);
        assertThat(response.hasMore()).isTrue();
        assertThat(response.nextCursor()).isEqualTo(new NotificacionCursor(fechaNueva, 20L));
        verify(notificacionRepository)
            .buscarPagina(eq(7L), isNull(), isNull(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(2);
    }

    @Test
    void listarPaginaPorUsuario_ultimaPaginaNoDevuelveCursor() {
        Notificacion notificacion = crearNotificacion(
            20L, LocalDateTime.of(2026, 11, 1, 12, 0));
        NotificacionCursor cursor = new NotificacionCursor(
            LocalDateTime.of(2026, 11, 1, 13, 0), 30L);
        when(notificacionRepository.buscarPagina(
            eq(7L), eq(cursor.fechaEnvio()), eq(cursor.idNotificacion()), any()))
            .thenReturn(List.of(notificacion));

        var response = service.listarPaginaPorUsuario(7L, cursor, 2);

        assertThat(response.content()).hasSize(1);
        assertThat(response.hasMore()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    private Notificacion crearNotificacion(Long id, LocalDateTime fechaEnvio) {
        Notificacion notificacion = new Notificacion();
        notificacion.setIdNotificacion(id);
        notificacion.setMensaje("Mensaje " + id);
        notificacion.setTipo("Info");
        notificacion.setLeido(false);
        notificacion.setFechaEnvio(fechaEnvio);
        return notificacion;
    }

    @Test
    void marcarLeida_soloEncuentraNotificacionesDelUsuario() {
        when(notificacionRepository.findByIdNotificacionAndUsuario_IdUsuario(11L, 7L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarLeida(11L, 7L))
            .isInstanceOf(NotFoundException.class);

        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void enviarRecordatorio_notificaSoloReservacionesActivasDelEventoPropio() {
        Organizador organizador = new Organizador();
        organizador.setIdUsuario(7L);
        organizador.setEmail("org@example.test");
        Evento evento = new Evento();
        evento.setIdEvento(12L);
        evento.setNombre("Conferencia");
        evento.setFecha(LocalDate.of(2026, 11, 1));
        evento.setOrganizador(organizador);

        Participante participante = new Participante();
        participante.setIdUsuario(30L);
        participante.setEmail("participante@example.test");
        Reservacion reservacion = new Reservacion();
        reservacion.setParticipante(participante);

        when(eventoRepository.findById(12L)).thenReturn(Optional.of(evento));
        when(organizadorRepository.findByEmail(organizador.getEmail())).thenReturn(Optional.of(organizador));
        when(reservacionRepository.findByEvento_IdEventoAndEstadoIn(
            12L, List.of("Pendiente", "Confirmada", "Pausada"))).thenReturn(List.of(reservacion));
        when(notificacionRepository.save(any(Notificacion.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.enviarRecordatorio(
            new NotificacionEnvioRequest(12L, null), organizador.getEmail(), false);

        assertThat(response.notificacionesEnviadas()).isEqualTo(1);
        verify(notificacionStompService).enviar(any(Notificacion.class));
    }

    @Test
    void enviarRecordatorio_rechazaEventoAjenoAntesDeNotificar() {
        Organizador dueño = new Organizador();
        dueño.setIdUsuario(8L);
        Evento evento = new Evento();
        evento.setIdEvento(12L);
        evento.setOrganizador(dueño);
        Organizador solicitante = new Organizador();
        solicitante.setIdUsuario(7L);

        when(eventoRepository.findById(12L)).thenReturn(Optional.of(evento));
        when(organizadorRepository.findByEmail("org@example.test")).thenReturn(Optional.of(solicitante));

        assertThatThrownBy(() -> service.enviarRecordatorio(
            new NotificacionEnvioRequest(12L, null), "org@example.test", false))
            .isInstanceOf(NotFoundException.class);

        verify(reservacionRepository, never()).findByEvento_IdEventoAndEstadoIn(any(), any());
        verify(notificacionRepository, never()).save(any());
    }
}
