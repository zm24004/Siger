package com.siger.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
