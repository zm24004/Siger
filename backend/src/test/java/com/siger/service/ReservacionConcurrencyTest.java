package com.siger.service;

import com.siger.TestContainersBase;
import com.siger.dto.ReservacionResponse;
import com.siger.exception.BusinessException;
import com.siger.model.Evento;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Ubicacion;
import com.siger.repository.AsistenciaRepository;
import com.siger.repository.EventoRepository;
import com.siger.repository.NotificacionRepository;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.ParticipanteRepository;
import com.siger.repository.ReservacionRepository;
import com.siger.repository.UbicacionRepository;
import com.siger.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de concurrencia que validan el bloqueo pesimista de
 * {@link ReservacionService#reservar}.
 *
 * <p>Usa {@code @SpringBootTest} (contexto completo) <b>sin</b>
 * {@code @Transactional} en la clase: asi cada invocacion de
 * {@code reservar()} desde un hilo del pool corre en su propia transaccion,
 * lo que permite ejercitar el {@code SELECT ... FOR UPDATE} real de MySQL/InnoDB
 * y comprobar que no se producen sobreocupaciones (sobrecupos).
 *
 * <p>El cerrojo pesimista garantiza que, frente a N reservas simultaneas sobre
 * un evento con capacidad C, exactamente C tienen exito y (N-C) fallan con
 * BusinessException "lleno". Sin el cerrojo habria condiciones de carrera y se
 * excederia el cupo.
 */
@SpringBootTest
class ReservacionConcurrencyTest extends TestContainersBase {

    @Autowired ReservacionService reservacionService;
    @Autowired EventoRepository eventoRepository;
    @Autowired ReservacionRepository reservacionRepository;
    @Autowired ParticipanteRepository participanteRepository;
    @Autowired OrganizadorRepository organizadorRepository;
    @Autowired UbicacionRepository ubicacionRepository;
    @Autowired NotificacionRepository notificacionRepository;
    @Autowired AsistenciaRepository asistenciaRepository;
    @Autowired UsuarioRepository usuarioRepository;

    private Long idEventoCap1;
    private Long idEventoCap3;
    private final List<String> emailsParticipantes = new ArrayList<>();

    @BeforeEach
    void sembrar() {
        limpiarTablas();

        Ubicacion ub = new Ubicacion();
        ub.setNombre("Auditorio concurrencia");
        ub.setDireccion("Dir concurrencia");
        ubicacionRepository.save(ub);

        Organizador org = new Organizador();
        org.setNombre("Org Concurrencia");
        org.setEmail("orgc@test.x");
        org.setContrasena("hash");
        org.setEstado(true);
        organizadorRepository.save(org);

        idEventoCap1 = persistirEvento("Evento cap 1", 1, ub, org);
        idEventoCap3 = persistirEvento("Evento cap 3", 3, ub, org);
    }

    @AfterEach
    void limpiar() {
        limpiarTablas();
    }

    // ------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------

    private Long persistirEvento(String nombre, int capacidad, Ubicacion ub, Organizador org) {
        Evento e = new Evento();
        e.setNombre(nombre);
        e.setDescripcion("Desc " + nombre);
        e.setFecha(LocalDate.now().plusDays(10));
        e.setHoraInicio(LocalTime.of(9, 0));
        e.setHoraFin(LocalTime.of(11, 0));
        e.setCapacidadMaxima(capacidad);
        e.setEstado("Activo");
        e.setUbicacion(ub);
        e.setOrganizador(org);
        return eventoRepository.save(e).getIdEvento();
    }

    private void crearParticipante(String sufijo) {
        Participante p = new Participante();
        p.setNombre("Part " + sufijo);
        String email = "conc" + sufijo + "@test.x";
        p.setEmail(email);
        p.setContrasena("hash");
        p.setEstado(true);
        participanteRepository.save(p);
        emailsParticipantes.add(email);
    }

    /** Limpia todas las tablas en orden respetando las FK (hijas antes que padres).
     *  Los subtipos (organizador/participante) se borran ANTES que la tabla raiz
     *  usuario, para que el constraint UNIQUE de email no choque entre tests. */
    private void limpiarTablas() {
        asistenciaRepository.deleteAll();
        reservacionRepository.deleteAll();
        notificacionRepository.deleteAll();
        eventoRepository.deleteAll();
        ubicacionRepository.deleteAll();
        organizadorRepository.deleteAll();
        participanteRepository.deleteAll();
        usuarioRepository.deleteAll();
        emailsParticipantes.clear();
    }

    /**
     * Ejecuta una reserva por cada email de forma concurrente, sincronizando el
     * arranque con un CountDownLatch para maximizar la superposicion. Devuelve
     * la lista de resultados (ReservacionResponse exitosa o RuntimeException).
     */
    private List<Object> ejecutarReservasConcurrentes(Long idEvento, List<String> emails) throws Exception {
        int n = emails.size();
        ExecutorService exec = Executors.newFixedThreadPool(n);
        CountDownLatch listos = new CountDownLatch(n);
        CountDownLatch disparar = new CountDownLatch(1);
        CountDownLatch fin = new CountDownLatch(n);

        List<Future<Object>> futuros = new ArrayList<>();
        for (String email : emails) {
            futuros.add(exec.submit(() -> {
                listos.countDown();
                try {
                    disparar.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return (Object) e;
                }
                try {
                    return (Object) reservacionService.reservar(idEvento, email);
                } catch (RuntimeException ex) {
                    return ex;
                } finally {
                    fin.countDown();
                }
            }));
        }

        // Esperar a que todos los hilos esten listos y dispararlos a la vez.
        assertThat(listos.await(10, TimeUnit.SECONDS))
                .as("los hilos no arrancaron a tiempo").isTrue();
        disparar.countDown();
        // Esperar el fin de todos (el cerrojo pesimista resuelve en ms, 60s de margen).
        assertThat(fin.await(60, TimeUnit.SECONDS))
                .as("los hilos no finalizaron a tiempo (posible deadlock").isTrue();
        exec.shutdown();

        List<Object> resultados = new ArrayList<>();
        for (Future<Object> f : futuros) {
            resultados.add(f.get(10, TimeUnit.SECONDS));
        }
        return resultados;
    }

    // ------------------------------------------------------------------
    //  Tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("2 reservas concurrentes sobre evento cap=1 -> exactamente 1 exito y 1 fallo por cupo lleno")
    void dosReservasConcurrentes_capacidad1_exactamenteUnExitoUnFallo() throws Exception {
        crearParticipante("A");
        crearParticipante("B");

        List<Object> resultados = ejecutarReservasConcurrentes(idEventoCap1, emailsParticipantes);

        long exitos = resultados.stream().filter(r -> r instanceof ReservacionResponse).count();
        long fallosLleno = resultados.stream()
                .filter(r -> r instanceof BusinessException b && b.getMessage().contains("lleno"))
                .count();

        assertThat(exitos).as("numero de reservas exitosas").isEqualTo(1);
        assertThat(fallosLleno).as("numero de fallos por cupo lleno").isEqualTo(1);
        assertThat(reservacionRepository.count())
                .as("reservas persistidas en BD").isEqualTo(1);
    }

    @Test
    @DisplayName("5 reservas concurrentes sobre evento cap=3 -> exactamente 3 exitos y 2 fallos (sin sobrecupo)")
    void cincoReservasConcurrentes_capacidad3_exactamenteTresExitos() throws Exception {
        for (String s : List.of("A", "B", "C", "D", "E")) {
            crearParticipante(s);
        }

        List<Object> resultados = ejecutarReservasConcurrentes(idEventoCap3, emailsParticipantes);

        long exitos = resultados.stream().filter(r -> r instanceof ReservacionResponse).count();
        long fallos = resultados.stream().filter(r -> r instanceof BusinessException).count();

        assertThat(exitos).as("reservas exitosas").isEqualTo(3);
        assertThat(fallos).as("reservas fallidas").isEqualTo(2);
        assertThat(reservacionRepository.count())
                .as("reservas persistidas (no debe exceder la capacidad)").isEqualTo(3);
    }
}
