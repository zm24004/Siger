package com.siger.service;

import com.siger.dto.AsistenciaRequest;
import com.siger.dto.AsistenciaResponse;
import com.siger.dto.AsistenciaUpdateRequest;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Asistencia;
import com.siger.model.Evento;
import com.siger.model.Reservacion;
import com.siger.repository.AsistenciaRepository;
import com.siger.repository.EventoRepository;
import com.siger.repository.ReservacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Records attendance for a reservation identified by its code.
 */
@Service
@RequiredArgsConstructor
public class AsistenciaService {

    private final ReservacionRepository reservacionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final EventoRepository eventoRepository;

    @Transactional
    public AsistenciaResponse registrar(AsistenciaRequest req, String email) {
        Reservacion r = reservacionRepository.findByCodigoReservacionForUpdate(req.codigoReservacion())
            .orElseThrow(() -> new NotFoundException("Reservación no encontrada con código: " + req.codigoReservacion()));

        if (!esOrganizadorDelEvento(r.getEvento(), email)) {
            throw new AccessDeniedException("Solo el organizador propietario puede registrar la asistencia.");
        }
        if (!"Confirmada".equalsIgnoreCase(r.getEstado())) {
            throw new BusinessException("La reservación no está confirmada (estado: " + r.getEstado() + ").");
        }

        if (asistenciaRepository.existsByReservacion_IdReservacion(r.getIdReservacion())) {
            throw new BusinessException("Ya se registró la asistencia para esta reservación.");
        }

        Asistencia a = new Asistencia();
        a.setReservacion(r);
        a.setFechaAsistencia(LocalDate.now());
        a.setHoraAsistencia(LocalTime.now());
        a.setEstado("Presente");
        Asistencia saved = asistenciaRepository.save(a);

        String eventoNombre = r.getEvento() != null ? r.getEvento().getNombre() : null;
        return toResponse(saved, eventoNombre);
    }

    @Transactional(readOnly = true)
    public List<AsistenciaResponse> listarPorEvento(Long idEvento, String email, boolean administrador) {
        Evento evento = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + idEvento));
        if (!administrador && !esOrganizadorDelEvento(evento, email)) {
            throw new AccessDeniedException("Solo el organizador propietario puede consultar la asistencia.");
        }
        return asistenciaRepository.findByReservacion_Evento_IdEventoOrderByFechaAsistenciaAscHoraAsistenciaAsc(idEvento)
            .stream().map(a -> toResponse(a, a.getReservacion().getEvento().getNombre())).toList();
    }

    @Transactional(readOnly = true)
    public AsistenciaResponse detalle(Long idAsistencia, String email, boolean administrador) {
        Asistencia asistencia = asistenciaRepository.findById(idAsistencia)
            .orElseThrow(() -> new NotFoundException("Asistencia no encontrada: " + idAsistencia));
        validarAcceso(asistencia, email, administrador);
        return toResponse(asistencia, asistencia.getReservacion().getEvento().getNombre());
    }

    @Transactional
    public AsistenciaResponse corregir(Long idAsistencia, AsistenciaUpdateRequest request,
                                       String email) {
        Asistencia asistencia = asistenciaRepository.findById(idAsistencia)
            .orElseThrow(() -> new NotFoundException("Asistencia no encontrada: " + idAsistencia));
        if (!esOrganizadorDelEvento(asistencia.getReservacion().getEvento(), email)) {
            throw new AccessDeniedException("Solo el organizador propietario puede corregir la asistencia.");
        }
        asistencia.setEstado(request.estado().equalsIgnoreCase("Presente") ? "Presente" : "Ausente");
        Asistencia saved = asistenciaRepository.save(asistencia);
        return toResponse(saved, saved.getReservacion().getEvento().getNombre());
    }

    private void validarAcceso(Asistencia asistencia, String email, boolean administrador) {
        if (administrador || esOrganizadorDelEvento(asistencia.getReservacion().getEvento(), email)) {
            return;
        }
        throw new AccessDeniedException("Solo el organizador propietario puede consultar o corregir la asistencia.");
    }

    private boolean esOrganizadorDelEvento(Evento evento, String email) {
        return evento.getOrganizador() != null
            && evento.getOrganizador().getEmail().equalsIgnoreCase(email);
    }

    private AsistenciaResponse toResponse(Asistencia asistencia, String eventoNombre) {
        Reservacion reservacion = asistencia.getReservacion();
        return new AsistenciaResponse(
            asistencia.getIdAsistencia(),
            asistencia.getEstado(),
            asistencia.getFechaAsistencia(),
            asistencia.getHoraAsistencia(),
            reservacion.getEvento().getIdEvento(),
            eventoNombre,
            reservacion.getCodigoReservacion(),
            reservacion.getParticipante().getNombre() + " "
                + (reservacion.getParticipante().getApellido() != null
                    ? reservacion.getParticipante().getApellido() : "")
        );
    }
}
