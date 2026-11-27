package com.siger.service;

import com.siger.dto.ReservacionResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ComprobantePdfServiceTest {

    private final ComprobantePdfService comprobantePdfService = new ComprobantePdfService();

    @Test
    void generarDevuelveUnDocumentoPdf() {
        ReservacionResponse reservacion = new ReservacionResponse(
            4L,
            "SIG-TEST04",
            "Confirmada",
            LocalDate.of(2026, 10, 27),
            12L,
            "Conferencia SIGER",
            LocalDate.of(2026, 11, 3),
            LocalTime.of(10, 30),
            "Ana Paz");

        byte[] pdf = comprobantePdfService.generar(reservacion);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
    }
}
