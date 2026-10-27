package com.siger.service;

import com.siger.model.Evento;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Reservacion;
import com.siger.repository.EventoRepository;
import com.siger.repository.NotificacionRepository;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.ParticipanteRepository;
import com.siger.repository.ReservacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservacionAccessTest {

    @Mock EventoRepository eventoRepository;
    @Mock ParticipanteRepository participanteRepository;
    @Mock OrganizadorRepository organizadorRepository;
    @Mock ReservacionRepository reservacionRepository;
    @Mock NotificacionRepository notificacionRepository;
    @Mock NotificacionStompService notificacionStompService;
    @InjectMocks ReservacionService reservacionService;

    @Test
    void listarRestringeLosResultadosDelOrganizadorASusEventos() {
        Organizador organizador = new Organizador();
        organizador.setIdUsuario(22L);
        when(organizadorRepository.findByEmail("org@example.test")).thenReturn(Optional.of(organizador));
        when(reservacionRepository.buscar(8L, "Confirmada", 22L)).thenReturn(List.of());

        assertThat(reservacionService.listar(8L, " Confirmada ", "org@example.test", false, true))
            .isEmpty();

        verify(reservacionRepository).buscar(8L, "Confirmada", 22L);
    }

    @Test
    void detalleRechazaLaReservacionDeOtroParticipante() {
        Organizador organizador = new Organizador();
        organizador.setEmail("owner@example.test");
        Evento evento = new Evento();
        evento.setOrganizador(organizador);

        Participante participante = new Participante();
        participante.setEmail("participant@example.test");

        Reservacion reservacion = new Reservacion();
        reservacion.setIdReservacion(3L);
        reservacion.setEvento(evento);
        reservacion.setParticipante(participante);
        when(reservacionRepository.findById(3L)).thenReturn(Optional.of(reservacion));

        assertThatThrownBy(() -> reservacionService.detalle("3", "outsider@example.test", false, false))
            .isInstanceOf(AccessDeniedException.class);
    }
}
