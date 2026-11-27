package com.siger.controller;

import com.siger.dto.EstadisticaDashboardResponse;
import com.siger.dto.EstadisticaParticipacionResponse;
import com.siger.dto.EstadisticaRankingResponse;
import com.siger.dto.EstadisticaReservacionesResponse;
import com.siger.dto.ReporteRequest;
import com.siger.dto.ReporteResponse;
import com.siger.security.Permisos;
import com.siger.service.EstadisticaService;
import com.siger.service.ReporteArchivo;
import com.siger.service.ReporteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1")
@PreAuthorize("hasAuthority('" + Permisos.EVENTS_MANAGE + "') or hasAuthority('" + Permisos.USERS_READ + "')")
public class ReporteController {

    private final ReporteService reporteService;
    private final EstadisticaService estadisticaService;

    public ReporteController(ReporteService reporteService, EstadisticaService estadisticaService) {
        this.reporteService = reporteService;
        this.estadisticaService = estadisticaService;
    }

    @GetMapping("/reportes")
    public ResponseEntity<List<ReporteResponse>> listarReportes() {
        return ResponseEntity.ok(reporteService.listar(
            SecurityUtils.requireEmail(), esAdministrador()));
    }

    @GetMapping("/reportes/{id}")
    public ResponseEntity<ReporteResponse> obtenerReporte(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(reporteService.obtener(
            id, SecurityUtils.requireEmail(), esAdministrador()));
    }

    @PostMapping("/reportes")
    public ResponseEntity<ReporteResponse> generarReporte(@Valid @RequestBody ReporteRequest request) {
        return ResponseEntity.status(201).body(reporteService.generar(
            request, SecurityUtils.requireEmail(), esAdministrador()));
    }

    @GetMapping("/reportes/{id}/exportar")
    public ResponseEntity<byte[]> exportarReporte(
        @PathVariable @Positive Long id,
        @RequestParam String formato
    ) {
        ReporteArchivo archivo = reporteService.exportar(
            id, formato, SecurityUtils.requireEmail(), esAdministrador());
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(archivo.contentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(archivo.nombre()).build().toString())
            .body(archivo.contenido());
    }

    @GetMapping("/estadisticas/participacion")
    public ResponseEntity<EstadisticaParticipacionResponse> participacion(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
        @RequestParam(required = false) @Positive Long idEvento
    ) {
        return ResponseEntity.ok(estadisticaService.participacion(
            SecurityUtils.requireEmail(), esAdministrador(), fechaDesde, fechaHasta, idEvento));
    }

    @GetMapping("/estadisticas/eventos-mas-concurridos")
    public ResponseEntity<EstadisticaRankingResponse> eventosMasConcurridos(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
        @RequestParam(required = false) @Positive Long idEvento,
        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limite
    ) {
        return ResponseEntity.ok(estadisticaService.ranking(
            SecurityUtils.requireEmail(), esAdministrador(), fechaDesde, fechaHasta, idEvento, limite));
    }

    @GetMapping("/estadisticas/reservaciones")
    public ResponseEntity<EstadisticaReservacionesResponse> reservaciones(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
        @RequestParam(required = false) @Positive Long idEvento
    ) {
        return ResponseEntity.ok(estadisticaService.reservaciones(
            SecurityUtils.requireEmail(), esAdministrador(), fechaDesde, fechaHasta, idEvento));
    }

    @GetMapping("/estadisticas/dashboard")
    public ResponseEntity<EstadisticaDashboardResponse> dashboard(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
        @RequestParam(required = false) @Positive Long idEvento
    ) {
        return ResponseEntity.ok(estadisticaService.dashboard(
            SecurityUtils.requireEmail(), esAdministrador(), fechaDesde, fechaHasta, idEvento));
    }

    private boolean esAdministrador() {
        return SecurityUtils.hasAuthority(Permisos.USERS_READ);
    }
}
