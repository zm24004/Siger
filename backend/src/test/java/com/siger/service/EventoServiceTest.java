package com.siger.service;

import com.siger.dto.EventoRecursoRequest;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Evento;
import com.siger.model.Notificacion;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Recurso;
import com.siger.model.Reservacion;
import com.siger.model.Ubicacion;
import com.siger.repository.EventoRecursoRepository;
import com.siger.repository.EventoRepository;
import com.siger.repository.NotificacionRepository;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.RecursoRepository;
import com.siger.repository.ReservacionRepository;
import com.siger.repository.UbicacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock EventoRepository eventoRepository;
    @Mock UbicacionRepository ubicacionRepository;
    @Mock OrganizadorRepository organizadorRepository;
    @Mock ReservacionRepository reservacionRepository;
    @Mock EventoRecursoRepository eventoRecursoRepository;
    @Mock RecursoRepository recursoRepository;
    @Mock NotificacionRepository notificacionRepository;
    @Mock NotificacionStompService notificacionStompService;

    @InjectMocks EventoService eventoService;

    @Test
    void cancelar_notificaUnaVezCadaParticipanteConReservacionActiva() {
        Organizador organizador = organizador(7L, "org@example.test");
        Evento evento = evento(15L, organizador);
        Participante participante = new Participante();
        participante.setIdUsuario(22L);
        participante.setEmail("participant@example.test");
        Reservacion reservacion = new Reservacion();
        reservacion.setParticipante(participante);

        when(organizadorRepository.findByEmail(organizador.getEmail())).thenReturn(Optional.of(organizador));
        when(eventoRepository.findByIdForUpdate(15L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(evento)).thenReturn(evento);
        when(reservacionRepository.contarActivas(15L)).thenReturn(1L);
        when(reservacionRepository.findByEvento_IdEventoAndEstadoIn(15L, List.of("Pendiente", "Confirmada", "Pausada")))
            .thenReturn(List.of(reservacion, reservacion));
        when(notificacionRepository.saveAndFlush(any(Notificacion.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(eventoService.cancelar(15L, organizador.getEmail()).estado()).isEqualTo("Cancelado");

        verify(notificacionRepository).saveAndFlush(any(Notificacion.class));
        verify(notificacionStompService).enviar(any(Notificacion.class));
    }

    @Test
    void cancelar_eventoDeOtroOrganizador_noModificaEvento() {
        Organizador organizador = organizador(7L, "org@example.test");
        Evento eventoAjeno = evento(15L, organizador(8L, "otro@example.test"));
        when(organizadorRepository.findByEmail(organizador.getEmail())).thenReturn(Optional.of(organizador));
        when(eventoRepository.findByIdForUpdate(15L)).thenReturn(Optional.of(eventoAjeno));

        assertThatThrownBy(() -> eventoService.cancelar(15L, organizador.getEmail()))
            .isInstanceOf(NotFoundException.class);

        verify(eventoRepository, never()).save(any(Evento.class));
        verify(notificacionRepository, never()).saveAndFlush(any(Notificacion.class));
    }

    @Test
    void asignarRecurso_rechazaUnidadesQueSuperanInventario() {
        Organizador organizador = organizador(7L, "org@example.test");
        Evento evento = evento(15L, organizador);
        Recurso recurso = new Recurso();
        recurso.setIdRecurso(9L);
        recurso.setCantidad(4);
        recurso.setEstado("Disponible");

        when(organizadorRepository.findByEmail(organizador.getEmail())).thenReturn(Optional.of(organizador));
        when(eventoRepository.findByIdForUpdate(15L)).thenReturn(Optional.of(evento));
        when(recursoRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(recurso));
        when(eventoRecursoRepository.existsById(any())).thenReturn(false);
        when(eventoRecursoRepository.cantidadAsignada(9L)).thenReturn(2L);

        assertThatThrownBy(() -> eventoService.asignarRecurso(
            15L,
            new EventoRecursoRequest(9L, 3),
            organizador.getEmail()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("unidades disponibles");

        verify(eventoRecursoRepository, never()).save(any());
    }

    private static Organizador organizador(Long id, String email) {
        Organizador organizador = new Organizador();
        organizador.setIdUsuario(id);
        organizador.setEmail(email);
        organizador.setNombre("Organizador");
        return organizador;
    }

    private static Evento evento(Long id, Organizador organizador) {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setIdUbicacion(4L);
        ubicacion.setNombre("Auditorio");

        Evento evento = new Evento();
        evento.setIdEvento(id);
        evento.setNombre("Conferencia");
        evento.setFecha(LocalDate.of(2099, 1, 1));
        evento.setHoraInicio(LocalTime.of(9, 0));
        evento.setCapacidadMaxima(10);
        evento.setEstado("Activo");
        evento.setUbicacion(ubicacion);
        evento.setOrganizador(organizador);
        return evento;
    }
}
