package com.siger.service;

import com.siger.dto.AsistenciaRequest;
import com.siger.dto.AsistenciaResponse;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Asistencia;
import com.siger.model.Reservacion;
import com.siger.repository.AsistenciaRepository;
import com.siger.repository.ReservacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Records attendance for a reservation identified by its code.
 */
@Service
@RequiredArgsConstructor
public class AsistenciaService {

    private final ReservacionRepository reservacionRepository;
    private final AsistenciaRepository asistenciaRepository;

    @Transactional
    public AsistenciaResponse registrar(AsistenciaRequest req) {
        Reservacion r = reservacionRepository.findByCodigoReservacion(req.codigoReservacion())
            .orElseThrow(() -> new NotFoundException("Reservación no encontrada con código: " + req.codigoReservacion()));

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
        return new AsistenciaResponse(
            saved.getIdAsistencia(),
            saved.getEstado(),
            saved.getFechaAsistencia(),
            saved.getHoraAsistencia(),
            eventoNombre
        );
    }
}
