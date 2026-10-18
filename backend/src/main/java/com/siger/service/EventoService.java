package com.siger.service;

import com.siger.dto.EventoRequest;
import com.siger.dto.EventoResponse;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Evento;
import com.siger.model.Organizador;
import com.siger.model.Ubicacion;
import com.siger.repository.EventoRepository;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.ReservacionRepository;
import com.siger.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * CRUD + catalogue operations for Evento.
 */
@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final OrganizadorRepository organizadorRepository;
    private final ReservacionRepository reservacionRepository;

    @Transactional(readOnly = true)
    public List<EventoResponse> catalogoActivos() {
        LocalDate hoy = LocalDate.now();
        return eventoRepository
            .findByEstadoAndFechaGreaterThanEqualOrderByFechaAsc("Activo", hoy)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse detalle(Long idEvento) {
        Evento e = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + idEvento));
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> misEventos(String emailOrganizador) {
        Organizador org = organizadorRepository.findByEmail(emailOrganizador)
            .orElseThrow(() -> new NotFoundException("Organizador no encontrado: " + emailOrganizador));
        return eventoRepository.findByOrganizador_IdUsuario(org.getIdUsuario())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public EventoResponse crear(EventoRequest req, String emailOrganizador) {
        Organizador org = organizadorRepository.findByEmail(emailOrganizador)
            .orElseThrow(() -> new NotFoundException("Organizador no encontrado: " + emailOrganizador));

        Ubicacion ub = ubicacionRepository.findById(req.idUbicacion())
            .orElseThrow(() -> new NotFoundException("Ubicación no encontrada: " + req.idUbicacion()));

        if (req.horaFin() != null && req.horaInicio() != null
            && req.horaFin().isBefore(req.horaInicio())) {
            throw new BusinessException("La hora de fin no puede ser anterior a la hora de inicio.");
        }

        Evento e = new Evento();
        e.setNombre(req.nombre());
        e.setDescripcion(req.descripcion());
        e.setFecha(req.fecha());
        e.setHoraInicio(req.horaInicio());
        e.setHoraFin(req.horaFin());
        e.setCapacidadMaxima(req.capacidadMaxima());
        e.setEstado("Activo");
        e.setUbicacion(ub);
        e.setOrganizador(org);

        Evento saved = eventoRepository.save(e);
        return toResponse(saved);
    }

    @Transactional
    public EventoResponse cancelar(Long idEvento) {
        Evento e = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + idEvento));
        if ("Cancelado".equalsIgnoreCase(e.getEstado())) {
            throw new BusinessException("El evento ya está cancelado.");
        }
        e.setEstado("Cancelado");
        Evento saved = eventoRepository.save(e);
        return toResponse(saved);
    }

    private EventoResponse toResponse(Evento e) {
        long ocupadas = reservacionRepository.contarActivas(e.getIdEvento());
        long disponibles = Math.max(0L, e.getCapacidadMaxima() - ocupadas);
        return new EventoResponse(
            e.getIdEvento(),
            e.getNombre(),
            e.getDescripcion(),
            e.getFecha(),
            e.getHoraInicio(),
            e.getHoraFin(),
            e.getCapacidadMaxima(),
            e.getEstado(),
            e.getUbicacion() != null ? e.getUbicacion().getIdUbicacion() : null,
            e.getUbicacion() != null ? e.getUbicacion().getNombre() : null,
            e.getOrganizador() != null ? e.getOrganizador().getIdUsuario() : null,
            e.getOrganizador() != null ? e.getOrganizador().getNombre() : null,
            disponibles
        );
    }
}
