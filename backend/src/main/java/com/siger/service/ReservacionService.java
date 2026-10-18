package com.siger.service;

import com.siger.dto.ReservacionResponse;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Evento;
import com.siger.model.Notificacion;
import com.siger.model.Participante;
import com.siger.model.Reservacion;
import com.siger.repository.EventoRepository;
import com.siger.repository.NotificacionRepository;
import com.siger.repository.ParticipanteRepository;
import com.siger.repository.ReservacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReservacionService {

    private final EventoRepository eventoRepository;
    private final ParticipanteRepository participanteRepository;
    private final ReservacionRepository reservacionRepository;
    private final NotificacionRepository notificacionRepository;
    private final NotificacionStompService notificacionStompService;

    @Transactional
    public ReservacionResponse reservar(Long idEvento, String emailParticipante) {
        Evento evento = eventoRepository.findByIdForUpdate(idEvento)
            .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + idEvento));
        Participante p = participanteRepository.findByEmail(emailParticipante)
            .orElseThrow(() -> new NotFoundException("Participante no encontrado: " + emailParticipante));
        if (!"Activo".equalsIgnoreCase(evento.getEstado()))
            throw new BusinessException("El evento no esta disponible para reservas.");
        if (reservacionRepository.existsByEvento_IdEventoAndParticipante_IdUsuario(idEvento, p.getIdUsuario()))
            throw new BusinessException("Ya tienes una reserva para este evento.");
        if (reservacionRepository.contarActivas(idEvento) >= evento.getCapacidadMaxima())
            throw new BusinessException("El evento esta lleno. No quedan cupos disponibles.");
        Reservacion r = new Reservacion();
        r.setEvento(evento);
        r.setParticipante(p);
        r.setCodigoReservacion(RolHelper.generarCodigoReservacion());
        r.setEstado("Confirmada");
        Reservacion saved = reservacionRepository.save(r);
        notificar(p, "Tu reservacion para '" + evento.getNombre() + "' fue confirmada. Codigo: " + saved.getCodigoReservacion());
        return toResponse(saved);
    }

    @Transactional
    public ReservacionResponse pausar(Long idReservacion, String emailParticipante) {
        return cambiarEstado(idReservacion, emailParticipante, "Pausada", Set.of("Confirmada"));
    }

    @Transactional
    public ReservacionResponse confirmar(Long idReservacion, String emailParticipante) {
        return cambiarEstado(idReservacion, emailParticipante, "Confirmada", Set.of("Pausada", "Pendiente"));
    }

    @Transactional
    public ReservacionResponse cancelar(Long idReservacion, String emailParticipante) {
        return cambiarEstado(idReservacion, emailParticipante, "Cancelada", Set.of("Confirmada", "Pausada", "Pendiente"));
    }

    @Transactional
    public ReservacionResponse cancelarComoAdmin(Long idReservacion) {
        Reservacion r = reservacionRepository.findById(idReservacion)
            .orElseThrow(() -> new NotFoundException("Reservacion no encontrada: " + idReservacion));
        if ("Cancelada".equals(r.getEstado()))
            throw new BusinessException("La reservacion ya esta cancelada.");
        r.setEstado("Cancelada");
        Reservacion saved = reservacionRepository.save(r);
        notificar(r.getParticipante(), "Tu reservacion para '" + r.getEvento().getNombre() + "' fue cancelada por el administrador.");
        return toResponse(saved);
    }

    private ReservacionResponse cambiarEstado(Long id, String email, String nuevoEstado, Set<String> validosDesde) {
        Reservacion r = reservacionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Reservacion no encontrada: " + id));
        if (!r.getParticipante().getEmail().equals(email))
            throw new BusinessException("No tienes permiso para modificar esta reservacion.");
        if (!validosDesde.contains(r.getEstado()))
            throw new BusinessException("No se puede cambiar a " + nuevoEstado + " desde el estado " + r.getEstado() + ".");
        r.setEstado(nuevoEstado);
        Reservacion saved = reservacionRepository.save(r);
        notificar(r.getParticipante(), "Tu reservacion para '" + r.getEvento().getNombre() + "' cambio a: " + nuevoEstado + ".");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReservacionResponse> misReservaciones(String emailParticipante) {
        Participante p = participanteRepository.findByEmail(emailParticipante)
            .orElseThrow(() -> new NotFoundException("Participante no encontrado: " + emailParticipante));
        return reservacionRepository.findByParticipante_IdUsuario(p.getIdUsuario()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservacionResponse> listarTodas() {
        return reservacionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReservacionResponse detallePorCodigo(String codigo) {
        return toResponse(reservacionRepository.findByCodigoReservacion(codigo)
            .orElseThrow(() -> new NotFoundException("Reservacion no encontrada con codigo: " + codigo)));
    }

    private void notificar(Participante p, String mensaje) {
        try {
            Notificacion n = new Notificacion();
            n.setUsuario(p);
            n.setMensaje(mensaje);
            n.setTipo("Info");
            n.setLeido(false);
            notificacionRepository.save(n);
            notificacionStompService.enviar(n);
        } catch (Exception ex) { }
    }

    private ReservacionResponse toResponse(Reservacion r) {
        Evento e = r.getEvento();
        return new ReservacionResponse(
            r.getIdReservacion(),
            r.getCodigoReservacion(),
            r.getEstado(),
            r.getFechaReservacion(),
            e != null ? e.getIdEvento() : null,
            e != null ? e.getNombre() : null,
            e != null ? e.getFecha() : null,
            e != null ? e.getHoraInicio() : null,
            r.getParticipante() != null ? r.getParticipante().getNombre() + " " + (r.getParticipante().getApellido() != null ? r.getParticipante().getApellido() : "") : null
        );
    }
}
