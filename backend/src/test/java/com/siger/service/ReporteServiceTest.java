package com.siger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siger.exception.NotFoundException;
import com.siger.model.Administrador;
import com.siger.model.Organizador;
import com.siger.model.Reporte;
import com.siger.repository.ReporteRepository;
import com.siger.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock ReporteRepository reporteRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ReporteScopeService scopeService;
    @Mock EstadisticaService estadisticaService;

    @InjectMocks ReporteService service;

    @Test
    void exportar_generaPdfYExcelDesdeElReportePersistido() {
        Reporte reporte = reporte();
        Administrador admin = (Administrador) reporte.getCreador();
        when(usuarioRepository.findByEmail("admin@example.test")).thenReturn(Optional.of(admin));
        when(reporteRepository.findById(5L)).thenReturn(Optional.of(reporte));
        service = new ReporteService(
            reporteRepository, usuarioRepository, scopeService, estadisticaService, new ObjectMapper());

        ReporteArchivo pdf = service.exportar(5L, "pdf", "admin@example.test", true);
        ReporteArchivo excel = service.exportar(5L, "excel", "admin@example.test", true);

        assertThat(pdf.nombre()).endsWith(".pdf");
        assertThat(pdf.contentType()).isEqualTo("application/pdf");
        assertThat(new String(pdf.contenido(), 0, 4)).isEqualTo("%PDF");
        assertThat(excel.nombre()).endsWith(".xlsx");
        assertThat(excel.contentType())
            .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(excel.contenido()).startsWith((byte) 'P', (byte) 'K');
    }

    @Test
    void obtener_ocultaReportesQueNoPertenecenAlOrganizador() {
        Organizador organizador = new Organizador();
        organizador.setIdUsuario(10L);
        organizador.setEmail("org@example.test");
        when(usuarioRepository.findByEmail("org@example.test")).thenReturn(Optional.of(organizador));
        when(reporteRepository.findByIdReporteAndCreador_IdUsuario(5L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(5L, "org@example.test", false))
            .isInstanceOf(NotFoundException.class);
    }

    private static Reporte reporte() {
        Administrador creador = new Administrador();
        creador.setIdUsuario(2L);
        creador.setEmail("admin@example.test");
        Reporte reporte = new Reporte();
        reporte.setIdReporte(5L);
        reporte.setTipo("PARTICIPACION");
        reporte.setContenido("{\"tipo\":\"PARTICIPACION\",\"eventos\":[]}");
        reporte.setCreador(creador);
        return reporte;
    }
}
