package com.siger.service;

import com.siger.exception.BadRequestException;
import com.siger.exception.NotFoundException;
import com.siger.model.Organizador;
import com.siger.repository.EventoRepository;
import com.siger.repository.OrganizadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReporteScopeService {

    private final OrganizadorRepository organizadorRepository;
    private final EventoRepository eventoRepository;

    public ReporteQueryScope validar(
        String email,
        boolean administrador,
        Long idEvento,
        LocalDate fechaDesde,
        LocalDate fechaHasta
    ) {
        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            throw new BadRequestException("fechaHasta no puede ser anterior a fechaDesde.");
        }
        Long idOrganizador = null;
        if (!administrador) {
            Organizador organizador = organizadorRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Organizador no encontrado: " + email));
            idOrganizador = organizador.getIdUsuario();
        }
        if (idEvento != null) {
            boolean eventoVisible = administrador
                ? eventoRepository.existsById(idEvento)
                : eventoRepository.findByIdEventoAndOrganizador_IdUsuario(idEvento, idOrganizador).isPresent();
            if (!eventoVisible) {
                throw new NotFoundException("Evento no encontrado: " + idEvento);
            }
        }
        return new ReporteQueryScope(idOrganizador, idEvento);
    }
}
