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
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.ParticipanteRepository;
import com.siger.repository.ReservacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReservacionService {

    private final EventoRepository eventoRepository;
    private final ParticipanteRepository participanteRepository;
    private final OrganizadorRepository organizadorRepository;
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

    @Transactional(readOnly = true)
    public List<ReservacionResponse> listar(Long idEvento, String estado, String email, boolean administrador,
                                            boolean organizador) {
        Long idOrganizador = null;
        if (!administrador) {
            if (!organizador) {
                throw new AccessDeniedException("No tienes permiso para consultar estas reservaciones.");
            }
            idOrganizador = organizadorRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Organizador no encontrado: " + email))
                .getIdUsuario();
        }
        String estadoFiltro = estado == null || estado.isBlank() ? null : estado.trim();
        return reservacionRepository.buscar(idEvento, estadoFiltro, idOrganizador).stream()
            .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservacionResponse> listarPorEvento(Long idEvento, String email, boolean administrador) {
        Evento evento = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + idEvento));
        if (!administrador && !esOrganizadorDelEvento(evento, email)) {
            throw new AccessDeniedException("Solo el organizador propietario puede consultar estas reservaciones.");
        }
        return reservacionRepository.findByEvento_IdEventoOrderByFechaReservacionAsc(idEvento).stream()
            .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservacionResponse> historialParticipante(Long idParticipante, String email, boolean administrador) {
        Participante participante = participanteRepository.findById(idParticipante)
            .orElseThrow(() -> new NotFoundException("Participante no encontrado: " + idParticipante));
        if (!administrador && !participante.getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Solo puedes consultar tu propio historial de reservaciones.");
        }
        return reservacionRepository.findByParticipante_IdUsuarioOrderByFechaReservacionDesc(idParticipante).stream()
            .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReservacionResponse detalle(String idOCodigo, String email, boolean administrador, boolean organizador) {
        Reservacion reservacion = encontrarPorIdOCodigo(idOCodigo);
        validarAcceso(reservacion, email, administrador, organizador);
        return toResponse(reservacion);
    }

    @Transactional(readOnly = true)
    public ReservacionResponse comprobante(Long idReservacion, String email) {
        Reservacion reservacion = reservacionRepository.findById(idReservacion)
            .orElseThrow(() -> new NotFoundException("Reservacion no encontrada: " + idReservacion));
        if (!reservacion.getParticipante().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Solo el participante propietario puede descargar el comprobante.");
        }
        return toResponse(reservacion);
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
    public ReservacionResponse cancelar(Long idReservacion, String email, boolean administrador, boolean organizador) {
        Reservacion reservacion = reservacionRepository.findById(idReservacion)
            .orElseThrow(() -> new NotFoundException("Reservacion no encontrada: " + idReservacion));
        validarAcceso(reservacion, email, administrador, organizador);
        if (!Set.of("Confirmada", "Pausada", "Pendiente").contains(reservacion.getEstado())) {
            throw new BusinessException("No se puede cancelar una reservacion en estado " + reservacion.getEstado() + ".");
        }
        reservacion.setEstado("Cancelada");
        Reservacion saved = reservacionRepository.save(reservacion);
        notificar(reservacion.getParticipante(),
            "Tu reservacion para '" + reservacion.getEvento().getNombre() + "' cambio a: Cancelada.");
        return toResponse(saved);
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

    private Reservacion encontrarPorIdOCodigo(String idOCodigo) {
        try {
            return reservacionRepository.findById(Long.parseLong(idOCodigo))
                .orElseThrow(() -> new NotFoundException("Reservacion no encontrada: " + idOCodigo));
        } catch (NumberFormatException ignored) {
            return reservacionRepository.findByCodigoReservacion(idOCodigo)
                .orElseThrow(() -> new NotFoundException("Reservacion no encontrada con codigo: " + idOCodigo));
        }
    }

    private void validarAcceso(Reservacion reservacion, String email, boolean administrador, boolean organizador) {
        if (administrador || reservacion.getParticipante().getEmail().equalsIgnoreCase(email)) {
            return;
        }
        if (organizador && esOrganizadorDelEvento(reservacion.getEvento(), email)) {
            return;
        }
        throw new AccessDeniedException("No tienes permiso para consultar o modificar esta reservacion.");
    }

    private boolean esOrganizadorDelEvento(Evento evento, String email) {
        return evento.getOrganizador() != null
            && evento.getOrganizador().getEmail().equalsIgnoreCase(email);
    }

    private void notificar(Participante p, String mensaje) {
        Notificacion n = new Notificacion();
        n.setUsuario(p);
        n.setMensaje(mensaje);
        n.setTipo("Info");
        n.setLeido(false);
        notificacionRepository.saveAndFlush(n);
        notificacionStompService.enviar(n);
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
