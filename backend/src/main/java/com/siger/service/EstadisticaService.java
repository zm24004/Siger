package com.siger.service;

import com.siger.dto.EstadisticaDashboardResponse;
import com.siger.dto.EstadisticaEventoResponse;
import com.siger.dto.EstadisticaParticipacionResponse;
import com.siger.dto.EstadisticaRankingResponse;
import com.siger.dto.EstadisticaReservacionesResponse;
import com.siger.dto.ReservacionesPorEstadoResponse;
import com.siger.repository.EventoRepository;
import com.siger.repository.ReservacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadisticaService {

    private final EventoRepository eventoRepository;
    private final ReservacionRepository reservacionRepository;
    private final ReporteScopeService scopeService;

    @Transactional(readOnly = true)
    public EstadisticaParticipacionResponse participacion(
        String email, boolean administrador, LocalDate fechaDesde, LocalDate fechaHasta, Long idEvento
    ) {
        List<EstadisticaEventoResponse> eventos = eventos(email, administrador, fechaDesde, fechaHasta, idEvento);
        return new EstadisticaParticipacionResponse(
            eventos,
            eventos.stream().mapToLong(EstadisticaEventoResponse::reservacionesActivas).sum(),
            eventos.stream().mapToLong(EstadisticaEventoResponse::asistentes).sum());
    }

    @Transactional(readOnly = true)
    public EstadisticaRankingResponse ranking(
        String email, boolean administrador, LocalDate fechaDesde, LocalDate fechaHasta, Long idEvento, int limite
    ) {
        List<EstadisticaEventoResponse> ranking = eventos(email, administrador, fechaDesde, fechaHasta, idEvento)
            .stream()
            .sorted(Comparator.comparing(EstadisticaEventoResponse::asistentes).reversed()
                .thenComparing(EstadisticaEventoResponse::reservacionesActivas, Comparator.reverseOrder())
                .thenComparing(EstadisticaEventoResponse::fecha)
                .thenComparing(EstadisticaEventoResponse::idEvento))
            .limit(limite)
            .toList();
        return new EstadisticaRankingResponse(ranking);
    }

    @Transactional(readOnly = true)
    public EstadisticaReservacionesResponse reservaciones(
        String email, boolean administrador, LocalDate fechaDesde, LocalDate fechaHasta, Long idEvento
    ) {
        ReporteQueryScope scope = scopeService.validar(email, administrador, idEvento, fechaDesde, fechaHasta);
        List<ReservacionesPorEstadoResponse> totales = reservacionRepository.contarPorEstadoParaReporte(
            scope.idEvento(), scope.idOrganizador(), fechaDesde, fechaHasta);
        return new EstadisticaReservacionesResponse(
            totales,
            totales.stream().mapToLong(ReservacionesPorEstadoResponse::total).sum());
    }

    @Transactional(readOnly = true)
    public EstadisticaDashboardResponse dashboard(
        String email, boolean administrador, LocalDate fechaDesde, LocalDate fechaHasta, Long idEvento
    ) {
        EstadisticaParticipacionResponse participacion =
            participacion(email, administrador, fechaDesde, fechaHasta, idEvento);
        double porcentaje = participacion.totalReservaciones() == 0
            ? 0
            : participacion.totalAsistentes() * 100.0 / participacion.totalReservaciones();
        return new EstadisticaDashboardResponse(
            participacion.eventos().size(),
            participacion.totalReservaciones(),
            participacion.totalAsistentes(),
            porcentaje);
    }

    @Transactional(readOnly = true)
    public List<EstadisticaEventoResponse> eventos(
        String email, boolean administrador, LocalDate fechaDesde, LocalDate fechaHasta, Long idEvento
    ) {
        ReporteQueryScope scope = scopeService.validar(email, administrador, idEvento, fechaDesde, fechaHasta);
        return eventoRepository.obtenerParticipacion(
            scope.idOrganizador(), fechaDesde, fechaHasta, scope.idEvento());
    }
}
