package com.siger.service;

import com.siger.dto.AsistenciaRequest;
import com.siger.dto.AsistenciaUpdateRequest;
import com.siger.exception.BusinessException;
import com.siger.model.Asistencia;
import com.siger.model.Evento;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Reservacion;
import com.siger.repository.AsistenciaRepository;
import com.siger.repository.EventoRepository;
import com.siger.repository.ReservacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsistenciaServiceTest {

    @Mock ReservacionRepository reservacionRepository;
    @Mock AsistenciaRepository asistenciaRepository;
    @Mock EventoRepository eventoRepository;
    @InjectMocks AsistenciaService asistenciaService;

    private Reservacion reservacion;

    @BeforeEach
    void setUp() {
        Organizador organizador = new Organizador();
        organizador.setEmail("organizador@example.test");

        Evento evento = new Evento();
        evento.setIdEvento(7L);
        evento.setNombre("Evento de prueba");
        evento.setOrganizador(organizador);

        Participante participante = new Participante();
        participante.setNombre("Ana");
        participante.setApellido("Paz");

        reservacion = new Reservacion();
        reservacion.setIdReservacion(9L);
        reservacion.setCodigoReservacion("SIG-TEST01");
        reservacion.setEstado("Confirmada");
        reservacion.setEvento(evento);
        reservacion.setParticipante(participante);
    }

    @Test
    void registrarAceptaReservaConfirmadaDelEventoPropio() {
        when(reservacionRepository.findByCodigoReservacionForUpdate("SIG-TEST01"))
            .thenReturn(Optional.of(reservacion));
        when(asistenciaRepository.existsByReservacion_IdReservacion(9L)).thenReturn(false);
        when(asistenciaRepository.save(any(Asistencia.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = asistenciaService.registrar(
            new AsistenciaRequest("SIG-TEST01"), "organizador@example.test");

        assertThat(response.estado()).isEqualTo("Presente");
        assertThat(response.idEvento()).isEqualTo(7L);
        assertThat(response.codigoReservacion()).isEqualTo("SIG-TEST01");
        assertThat(response.participanteNombre()).isEqualTo("Ana Paz");
        verify(reservacionRepository).findByCodigoReservacionForUpdate("SIG-TEST01");
    }

    @Test
    void registrarRechazaReservaNoConfirmada() {
        reservacion.setEstado("Cancelada");
        when(reservacionRepository.findByCodigoReservacionForUpdate("SIG-TEST01"))
            .thenReturn(Optional.of(reservacion));

        assertThatThrownBy(() -> asistenciaService.registrar(
            new AsistenciaRequest("SIG-TEST01"), "organizador@example.test"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("no está confirmada");
    }

    @Test
    void registrarRechazaOrganizadorAjeno() {
        when(reservacionRepository.findByCodigoReservacionForUpdate("SIG-TEST01"))
            .thenReturn(Optional.of(reservacion));

        assertThatThrownBy(() -> asistenciaService.registrar(
            new AsistenciaRequest("SIG-TEST01"), "otro@example.test"))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void registrarRechazaUnaAsistenciaDuplicada() {
        when(reservacionRepository.findByCodigoReservacionForUpdate("SIG-TEST01"))
            .thenReturn(Optional.of(reservacion));
        when(asistenciaRepository.existsByReservacion_IdReservacion(9L)).thenReturn(true);

        assertThatThrownBy(() -> asistenciaService.registrar(
            new AsistenciaRequest("SIG-TEST01"), "organizador@example.test"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Ya se registró");
    }

    @Test
    void corregirPermiteOrganizadorPropietarioCambiarEstadoAAusente() {
        Asistencia asistencia = new Asistencia();
        asistencia.setIdAsistencia(14L);
        asistencia.setReservacion(reservacion);
        when(asistenciaRepository.findById(14L)).thenReturn(Optional.of(asistencia));
        when(asistenciaRepository.save(asistencia)).thenReturn(asistencia);

        var response = asistenciaService.corregir(
            14L,
            new AsistenciaUpdateRequest("ausente"),
            "organizador@example.test");

        assertThat(response.estado()).isEqualTo("Ausente");
    }

    @Test
    void corregirRechazaAUnOrganizadorAjeno() {
        Asistencia asistencia = new Asistencia();
        asistencia.setReservacion(reservacion);
        when(asistenciaRepository.findById(14L)).thenReturn(Optional.of(asistencia));

        assertThatThrownBy(() -> asistenciaService.corregir(
            14L,
            new AsistenciaUpdateRequest("Ausente"),
            "otro@example.test"))
            .isInstanceOf(AccessDeniedException.class);
    }
}
