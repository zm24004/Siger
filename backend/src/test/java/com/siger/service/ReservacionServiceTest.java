package com.siger.service;

import com.siger.TestContainersBase;
import com.siger.dto.ReservacionResponse;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Evento;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Reservacion;
import com.siger.model.Ubicacion;
import com.siger.repository.EventoRepository;
import com.siger.repository.NotificacionRepository;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.ParticipanteRepository;
import com.siger.repository.ReservacionRepository;
import com.siger.repository.UbicacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas de la logica de {@link ReservacionService} contra un MySQL real
 * levantado con Testcontainers.
 *
 * <p>Usa {@code @DataJpaTest} (slice JPA) + {@code @Import} del servicio. Cada
 * test se ejecuta dentro de una transaccion que se deshace al final (rollback
 * automatico), garantizando aislamiento entre pruebas.
 *
 * <p>Se cubren los casos: happy path (con generacion de codigo y notificacion),
 * NotFoundException (evento/participante inexistentes), BusinessException
 * (evento cancelado, duplicado, cupo lleno) y consultas (misReservaciones,
 * detallePorCodigo).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ReservacionService.class)
class ReservacionServiceTest extends TestContainersBase {

    @Autowired ReservacionService reservacionService;
    @Autowired EventoRepository eventoRepository;
    @Autowired ReservacionRepository reservacionRepository;
    @Autowired ParticipanteRepository participanteRepository;
    @Autowired OrganizadorRepository organizadorRepository;
    @Autowired UbicacionRepository ubicacionRepository;
    @Autowired NotificacionRepository notificacionRepository;
    /** El servicio STOMP no se carga en el slice @DataJpaTest; se moquea para
     *  que {@link ReservacionService} pueda inyectarlo (su push es best-effort). */
    @MockitoBean NotificacionStompService notificacionStompService;

    private Ubicacion ubicacion;
    private Organizador organizador;
    private Participante participanteA;
    private Participante participanteB;
    private Evento eventoCap1;
    private Evento eventoCap2;

    @BeforeEach
    void sembrar() {
        ubicacion = new Ubicacion();
        ubicacion.setNombre("Auditorio test");
        ubicacion.setDireccion("Campus test");
        ubicacionRepository.save(ubicacion);

        organizador = new Organizador();
        organizador.setNombre("Org Test");
        organizador.setEmail("org@test.x");
        organizador.setContrasena("hash-bcrypt-no-importa-en-tests");
        organizador.setEstado(true);
        organizadorRepository.save(organizador);

        participanteA = newParticipante("Part A", "partA@test.x");
        participanteB = newParticipante("Part B", "partB@test.x");

        eventoCap1 = persistirEvento("Evento cap 1", 1, "Activo");
        eventoCap2 = persistirEvento("Evento cap 2", 2, "Activo");
    }

    // ------------------------------------------------------------------
    //  Helpers de siembra
    // ------------------------------------------------------------------

    private Participante newParticipante(String nombre, String email) {
        Participante p = new Participante();
        p.setNombre(nombre);
        p.setEmail(email);
        p.setContrasena("hash-bcrypt-no-importa-en-tests");
        p.setEstado(true);
        return participanteRepository.save(p);
    }

    private Evento persistirEvento(String nombre, int capacidad, String estado) {
        Evento e = new Evento();
        e.setNombre(nombre);
        e.setDescripcion("Descripcion de " + nombre);
        e.setFecha(LocalDate.now().plusDays(7));
        e.setHoraInicio(LocalTime.of(9, 0));
        e.setHoraFin(LocalTime.of(12, 0));
        e.setCapacidadMaxima(capacidad);
        e.setEstado(estado);
        e.setUbicacion(ubicacion);
        e.setOrganizador(organizador);
        return eventoRepository.save(e);
    }

    private Reservacion reservacionPrevia(Evento e, Participante p, String codigo, String estado) {
        Reservacion r = new Reservacion();
        r.setEvento(e);
        r.setParticipante(p);
        r.setCodigoReservacion(codigo);
        r.setEstado(estado);
        return reservacionRepository.save(r);
    }

    // ------------------------------------------------------------------
    //  Happy path
    // ------------------------------------------------------------------

    @Test
    @DisplayName("reservar: todo valido -> retorna reservacion Confirmada con codigo SIG-")
    void reservar_cuandoTodoValido_retornaReservacionConfirmadaConCodigo() {
        ReservacionResponse r = reservacionService.reservar(eventoCap2.getIdEvento(), participanteA.getEmail());

        assertThat(r).isNotNull();
        assertThat(r.estado()).as("estado de la reserva").isEqualTo("Confirmada");
        assertThat(r.codigoReservacion()).as("codigo generado").startsWith("SIG-");
        assertThat(r.idEvento()).isEqualTo(eventoCap2.getIdEvento());
        assertThat(reservacionRepository.count()).as("reservas en BD").isEqualTo(1);
    }

    @Test
    @DisplayName("reservar: crea una notificacion no leida para el participante")
    void reservar_cuandoTodoValido_creaNotificacionParaParticipante() {
        reservacionService.reservar(eventoCap2.getIdEvento(), participanteA.getEmail());

        long noLeidas = notificacionRepository
                .countByUsuario_IdUsuarioAndLeidoFalse(participanteA.getIdUsuario());

        assertThat(noLeidas).as("notificaciones no leidas del participante A").isEqualTo(1);
    }

    // ------------------------------------------------------------------
    //  NotFoundException
    // ------------------------------------------------------------------

    @Test
    @DisplayName("reservar: evento inexistente -> NotFoundException")
    void reservar_eventoInexistente_lanzaNotFound() {
        assertThatThrownBy(() -> reservacionService.reservar(99999L, participanteA.getEmail()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Evento no encontrado");
    }

    @Test
    @DisplayName("reservar: participante inexistente -> NotFoundException")
    void reservar_participanteInexistente_lanzaNotFound() {
        assertThatThrownBy(() -> reservacionService.reservar(eventoCap2.getIdEvento(), "ghost@test.x"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Participante no encontrado");
    }

    // ------------------------------------------------------------------
    //  BusinessException
    // ------------------------------------------------------------------

    @Test
    @DisplayName("reservar: evento Cancelado -> BusinessException (no disponible)")
    void reservar_eventoCancelado_lanzaBusiness() {
        Evento cancelado = persistirEvento("Evento cancelado", 5, "Cancelado");

        assertThatThrownBy(() -> reservacionService.reservar(cancelado.getIdEvento(), participanteA.getEmail()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("disponible");
    }

    @Test
    @DisplayName("reservar: participante ya tiene reserva -> BusinessException (duplicado)")
    void reservar_participanteDuplicado_lanzaBusiness() {
        reservacionPrevia(eventoCap2, participanteA, "SIG-DUP0001", "Confirmada");

        assertThatThrownBy(() -> reservacionService.reservar(eventoCap2.getIdEvento(), participanteA.getEmail()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ya tienes una reserva");
    }

    @Test
    @DisplayName("reservar: cupo lleno con otro participante -> BusinessException (lleno)")
    void reservar_cupoLleno_lanzaBusiness() {
        // Evento cap=1 ya reservado por A; B no encuentra cupo.
        reservacionPrevia(eventoCap1, participanteA, "SIG-FUL0001", "Confirmada");

        assertThatThrownBy(() -> reservacionService.reservar(eventoCap1.getIdEvento(), participanteB.getEmail()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("lleno");
    }

    // ------------------------------------------------------------------
    //  Consultas
    // ------------------------------------------------------------------

    @Test
    @DisplayName("misReservaciones: devuelve solo las reservas del participante indicado")
    void misReservaciones_devuelveSoloLasDelParticipante() {
        reservacionPrevia(eventoCap2, participanteA, "SIG-MIS001", "Confirmada");
        reservacionPrevia(eventoCap1, participanteB, "SIG-MIS002", "Confirmada");

        var deA = reservacionService.misReservaciones(participanteA.getEmail());
        var deB = reservacionService.misReservaciones(participanteB.getEmail());

        assertThat(deA).as("reservas de A").hasSize(1);
        assertThat(deA.get(0).codigoReservacion()).isEqualTo("SIG-MIS001");

        assertThat(deB).as("reservas de B").hasSize(1);
        assertThat(deB.get(0).codigoReservacion()).isEqualTo("SIG-MIS002");
    }

    @Test
    @DisplayName("detallePorCodigo: existe -> retorna la reservacion")
    void detallePorCodigo_existente_retornaReservacion() {
        reservacionPrevia(eventoCap2, participanteA, "SIG-DET001", "Confirmada");

        ReservacionResponse r = reservacionService.detallePorCodigo("SIG-DET001");

        assertThat(r).isNotNull();
        assertThat(r.codigoReservacion()).isEqualTo("SIG-DET001");
        assertThat(r.idEvento()).isEqualTo(eventoCap2.getIdEvento());
    }

    @Test
    @DisplayName("detallePorCodigo: no existe -> NotFoundException")
    void detallePorCodigo_inexistente_lanzaNotFound() {
        assertThatThrownBy(() -> reservacionService.detallePorCodigo("SIG-NOEXIST"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("no encontrada");
    }
}
