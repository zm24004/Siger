package com.siger.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.siger.dto.EstadisticaEventoResponse;
import com.siger.dto.ReporteRequest;
import com.siger.dto.ReporteResponse;
import com.siger.exception.BadRequestException;
import com.siger.exception.NotFoundException;
import com.siger.model.Reporte;
import com.siger.model.Usuario;
import com.siger.repository.ReporteRepository;
import com.siger.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private static final List<String> TIPOS = List.of(
        "PARTICIPACION", "EVENTOS_MAS_CONCURRIDOS", "RESERVACIONES");

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReporteScopeService scopeService;
    private final EstadisticaService estadisticaService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ReporteResponse> listar(String email, boolean administrador) {
        Usuario usuario = usuario(email);
        List<Reporte> reportes = administrador
            ? reporteRepository.findAllByOrderByFechaGeneracionDesc()
            : reporteRepository.findByCreador_IdUsuarioOrderByFechaGeneracionDesc(usuario.getIdUsuario());
        return reportes.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReporteResponse obtener(Long idReporte, String email, boolean administrador) {
        return toResponse(findAccessible(idReporte, email, administrador));
    }

    @Transactional
    public ReporteResponse generar(ReporteRequest request, String email, boolean administrador) {
        String tipo = request.tipo().trim().toUpperCase(Locale.ROOT);
        if (!TIPOS.contains(tipo)) {
            throw new BadRequestException("tipo debe ser PARTICIPACION, EVENTOS_MAS_CONCURRIDOS o RESERVACIONES.");
        }
        Usuario creador = usuario(email);
        scopeService.validar(email, administrador, request.idEvento(), request.fechaDesde(), request.fechaHasta());

        Map<String, Object> filtros = new LinkedHashMap<>();
        filtros.put("fechaDesde", request.fechaDesde());
        filtros.put("fechaHasta", request.fechaHasta());
        filtros.put("idEvento", request.idEvento());

        Map<String, Object> contenido = new LinkedHashMap<>();
        contenido.put("tipo", tipo);
        contenido.put("filtros", filtros);
        if ("RESERVACIONES".equals(tipo)) {
            var reservaciones = estadisticaService.reservaciones(
                email, administrador, request.fechaDesde(), request.fechaHasta(), request.idEvento());
            contenido.put("porEstado", reservaciones.porEstado());
            contenido.put("totalReservaciones", reservaciones.total());
        } else {
            List<EstadisticaEventoResponse> eventos = estadisticaService.eventos(
                email, administrador, request.fechaDesde(), request.fechaHasta(), request.idEvento());
            if ("EVENTOS_MAS_CONCURRIDOS".equals(tipo)) {
                eventos = eventos.stream()
                    .sorted(Comparator.comparing(EstadisticaEventoResponse::asistentes).reversed()
                        .thenComparing(EstadisticaEventoResponse::reservacionesActivas, Comparator.reverseOrder())
                        .thenComparing(EstadisticaEventoResponse::fecha))
                    .toList();
            }
            contenido.put("eventos", eventos);
        }

        Reporte reporte = new Reporte();
        reporte.setTipo(tipo);
        reporte.setParametro(json(filtros));
        reporte.setContenido(json(contenido));
        reporte.setCreador(creador);
        return toResponse(reporteRepository.save(reporte));
    }

    @Transactional(readOnly = true)
    public ReporteArchivo exportar(Long idReporte, String formato, String email, boolean administrador) {
        Reporte reporte = findAccessible(idReporte, email, administrador);
        JsonNode data = parse(reporte.getContenido());
        String nombreBase = "reporte-" + reporte.getIdReporte();
        if ("pdf".equalsIgnoreCase(formato)) {
            return new ReporteArchivo(nombreBase + ".pdf", "application/pdf", aPdf(reporte, data));
        }
        if ("excel".equalsIgnoreCase(formato)) {
            return new ReporteArchivo(
                nombreBase + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                aExcel(reporte, data));
        }
        throw new BadRequestException("formato debe ser pdf o excel.");
    }

    private Reporte findAccessible(Long idReporte, String email, boolean administrador) {
        Usuario usuario = usuario(email);
        return (administrador
            ? reporteRepository.findById(idReporte)
            : reporteRepository.findByIdReporteAndCreador_IdUsuario(idReporte, usuario.getIdUsuario()))
            .orElseThrow(() -> new NotFoundException("Reporte no encontrado: " + idReporte));
    }

    private Usuario usuario(String email) {
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado: " + email));
    }

    private ReporteResponse toResponse(Reporte reporte) {
        Usuario creador = reporte.getCreador();
        return new ReporteResponse(
            reporte.getIdReporte(),
            reporte.getTipo(),
            reporte.getParametro(),
            reporte.getContenido(),
            reporte.getFechaGeneracion(),
            creador.getIdUsuario(),
            creador.getNombre() + " " + creador.getApellido());
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el contenido del reporte.", ex);
        }
    }

    private JsonNode parse(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("El reporte no contiene datos exportables.");
        }
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("El contenido persistido del reporte no es JSON válido.", ex);
        }
    }

    private byte[] aPdf(Reporte reporte, JsonNode data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, output);
            document.open();
            document.add(new Paragraph("Reporte SIGER: " + reporte.getTipo(), new Font(Font.HELVETICA, 14, Font.BOLD)));
            document.add(new Paragraph("Generado: " + reporte.getFechaGeneracion()));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.addCell(new Phrase("Campo", new Font(Font.HELVETICA, 10, Font.BOLD)));
            table.addCell(new Phrase("Valor", new Font(Font.HELVETICA, 10, Font.BOLD)));
            data.fields().forEachRemaining(field -> {
                table.addCell(field.getKey());
                table.addCell(field.getValue().isContainerNode()
                    ? field.getValue().toPrettyString()
                    : field.getValue().asText());
            });
            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte.", ex);
        }
    }

    private byte[] aExcel(Reporte reporte, JsonNode data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte");
            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue("Reporte SIGER: " + reporte.getTipo());
            int rowNumber = 1;
            for (var fields = data.fields(); fields.hasNext();) {
                var field = fields.next();
                Row row = sheet.createRow(rowNumber++);
                row.createCell(0).setCellValue(field.getKey());
                row.createCell(1).setCellValue(field.getValue().isContainerNode()
                    ? field.getValue().toPrettyString()
                    : field.getValue().asText());
            }
            sheet.autoSizeColumn(0);
            sheet.setColumnWidth(1, 12000);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el Excel del reporte.", ex);
        }
    }
}
