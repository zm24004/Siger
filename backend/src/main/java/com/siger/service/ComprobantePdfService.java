package com.siger.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.siger.dto.ReservacionResponse;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ComprobantePdfService {

    public byte[] generar(ReservacionResponse reservacion) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 48, 48, 48, 48);
        try {
            PdfWriter.getInstance(document, output);
            document.open();
            document.add(new Paragraph("Comprobante de reservación", new Font(Font.HELVETICA, 18, Font.BOLD)));
            document.add(new Paragraph("Código: " + reservacion.codigoReservacion()));
            document.add(new Paragraph("Evento: " + reservacion.eventoNombre()));
            document.add(new Paragraph("Fecha: " + reservacion.eventoFecha()));
            document.add(new Paragraph("Hora: " + reservacion.eventoHoraInicio()));
            document.add(new Paragraph("Participante: " + reservacion.participanteNombre()));
            document.add(new Paragraph("Estado: " + reservacion.estado()));
            document.add(new Paragraph("Fecha de reservación: " + reservacion.fechaReservacion()));
            document.close();
            return output.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el comprobante PDF.", e);
        }
    }
}
