package com.siger.service;

import com.siger.dto.EstadisticaEventoResponse;
import com.siger.dto.ReservacionesPorEstadoResponse;
import com.siger.repository.EventoRepository;
import com.siger.repository.ReservacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadisticaServiceTest {

    @Mock EventoRepository eventoRepository;
    @Mock ReservacionRepository reservacionRepository;
    @Mock ReporteScopeService scopeService;

    @InjectMocks EstadisticaService service;

    @Test
    void participacion_sumaReservacionesYAsistenciasDeEventosVisibles() {
        List<EstadisticaEventoResponse> eventos = List.of(
            evento(1L, 12L, 8L),
            evento(2L, 7L, 4L));
        when(scopeService.validar("org@example.test", false, null, null, null))
            .thenReturn(new ReporteQueryScope(9L, null));
        when(eventoRepository.obtenerParticipacion(9L, null, null, null)).thenReturn(eventos);

        var response = service.participacion("org@example.test", false, null, null, null);

        assertThat(response.eventos()).hasSize(2);
        assertThat(response.totalReservaciones()).isEqualTo(19);
        assertThat(response.totalAsistentes()).isEqualTo(12);
    }

    @Test
    void ranking_ordenaPorAsistenciaYAplicaLimite() {
        when(scopeService.validar("admin@example.test", true, null, null, null))
            .thenReturn(new ReporteQueryScope(null, null));
        when(eventoRepository.obtenerParticipacion(null, null, null, null)).thenReturn(List.of(
            evento(1L, 5L, 2L),
            evento(2L, 8L, 7L),
            evento(3L, 20L, 10L)));

        var response = service.ranking("admin@example.test", true, null, null, null, 2);

        assertThat(response.eventos()).extracting(EstadisticaEventoResponse::idEvento)
            .containsExactly(3L, 2L);
    }

    @Test
    void reservaciones_sumaEstadosIncluyendoCanceladas() {
        when(scopeService.validar("admin@example.test", true, null, null, null))
            .thenReturn(new ReporteQueryScope(null, null));
        when(reservacionRepository.contarPorEstadoParaReporte(null, null, null, null))
            .thenReturn(List.of(
                new ReservacionesPorEstadoResponse("Cancelada", 2L),
                new ReservacionesPorEstadoResponse("Confirmada", 5L)));

        var response = service.reservaciones("admin@example.test", true, null, null, null);

        assertThat(response.total()).isEqualTo(7);
        assertThat(response.porEstado()).hasSize(2);
    }

    private static EstadisticaEventoResponse evento(Long id, Long reservaciones, Long asistentes) {
        return new EstadisticaEventoResponse(
            id, "Evento " + id, LocalDate.of(2026, 11, 1), 100, reservaciones, asistentes);
    }
}
