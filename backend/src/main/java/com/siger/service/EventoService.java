package com.siger.service;

import com.siger.dto.EventoDisponibilidadResponse;
import com.siger.dto.EventoRecursoRequest;
import com.siger.dto.EventoRecursoResponse;
import com.siger.dto.EventoRequest;
import com.siger.dto.EventoResponse;
import com.siger.dto.PageResponse;
import com.siger.dto.ReprogramarEventoRequest;
import com.siger.exception.BadRequestException;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Evento;
import com.siger.model.EventoRecurso;
import com.siger.model.EventoRecursoId;
import com.siger.model.Notificacion;
import com.siger.model.Organizador;
import com.siger.model.Recurso;
import com.siger.model.Reservacion;
import com.siger.model.Ubicacion;
import com.siger.repository.EventoRecursoRepository;
import com.siger.repository.EventoRepository;
import com.siger.repository.NotificacionRepository;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.RecursoRepository;
import com.siger.repository.ReservacionRepository;
import com.siger.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Event catalogue and organizer-scoped event operations.
 */
@Service
@RequiredArgsConstructor
public class EventoService {

    private static final List<String> RESERVACIONES_ACTIVAS = List.of("Pendiente", "Confirmada", "Pausada");

    private final EventoRepository eventoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final OrganizadorRepository organizadorRepository;
    private final ReservacionRepository reservacionRepository;
    private final EventoRecursoRepository eventoRecursoRepository;
    private final RecursoRepository recursoRepository;
    private final NotificacionRepository notificacionRepository;
    private final NotificacionStompService notificacionStompService;

    @Transactional(readOnly = true)
    public PageResponse<EventoResponse> catalogoActivos(Integer page, Integer limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit);
        Page<Evento> eventosActivos = eventoRepository.findByEstado("Activo", pageable);
        return new PageResponse<EventoResponse>(
            eventosActivos.getContent().stream().map(this::toResponse).toList(),
            page,
            limit,
            eventosActivos.getTotalElements(),
            eventosActivos.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PageResponse<EventoResponse> eventosPopulares(Integer page, Integer limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit);

        Page<Evento> eventosPopulares = eventoRepository.findPopularActiveEvents(pageable);
        return new PageResponse<EventoResponse>(
            eventosPopulares.getContent().stream().map(this::toResponse).toList(),
            page,
            limit,
            eventosPopulares.getTotalElements(),
            eventosPopulares.getTotalPages());
    }


    @Transactional(readOnly = true)
    public List<EventoResponse> catalogo(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String estado,
        Long idUbicacion
    ) {
        String estadoFiltro = estado == null || estado.isBlank() ? "Activo" : estado.trim();
        LocalDate desdeFiltro = fechaDesde == null && fechaHasta == null ? LocalDate.now() : fechaDesde;
        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            throw new BadRequestException("La fechaHasta no puede ser anterior a fechaDesde.");
        }
        return eventoRepository.buscarCatalogo(estadoFiltro, desdeFiltro, fechaHasta, idUbicacion)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse detalle(Long idEvento) {
        return toResponse(findEvent(idEvento));
    }

    @Transactional(readOnly = true)
    public EventoDisponibilidadResponse disponibilidad(Long idEvento) {
        Evento evento = findEvent(idEvento);
        long ocupadas = reservacionRepository.contarActivas(idEvento);
        return new EventoDisponibilidadResponse(
            idEvento,
            evento.getCapacidadMaxima(),
            ocupadas,
            cuposDisponibles(evento, ocupadas));
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> misEventos(String emailOrganizador) {
        Organizador organizador = organizadorRepository.findByEmail(emailOrganizador)
            .orElseThrow(() -> new NotFoundException("Organizador no encontrado: " + emailOrganizador));
        return eventoRepository.findByOrganizador_IdUsuario(organizador.getIdUsuario())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> eventosDeOrganizador(Long idOrganizador, String emailSolicitante, boolean administrador) {
        Organizador organizador = organizadorRepository.findById(idOrganizador)
            .orElseThrow(() -> new NotFoundException("Organizador no encontrado: " + idOrganizador));
        if (!administrador) {
            Organizador solicitante = organizadorRepository.findByEmail(emailSolicitante)
                .orElseThrow(() -> new NotFoundException("Organizador no encontrado: " + emailSolicitante));
            if (!solicitante.getIdUsuario().equals(organizador.getIdUsuario())) {
                throw new BusinessException("Solo puedes consultar los eventos de tu propia cuenta.");
            }
        }
        return eventoRepository.findByOrganizador_IdUsuario(organizador.getIdUsuario())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public EventoResponse crear(EventoRequest req, String emailOrganizador) {
        Organizador organizador = organizadorRepository.findByEmail(emailOrganizador)
            .orElseThrow(() -> new NotFoundException("Organizador no encontrado: " + emailOrganizador));
        Ubicacion ubicacion = findLocation(req.idUbicacion());
        validarHorario(req.horaInicio(), req.horaFin());

        Evento evento = new Evento();
        evento.setOrganizador(organizador);
        evento.setEstado("Activo");
        aplicar(evento, req, ubicacion);
        return toResponse(eventoRepository.save(evento));
    }

    @Transactional
    public EventoResponse actualizar(Long idEvento, EventoRequest req, String emailOrganizador) {
        Evento evento = findOwnedEventForUpdate(idEvento, emailOrganizador);
        validarEventoModificable(evento);
        Ubicacion ubicacion = findLocation(req.idUbicacion());
        validarHorario(req.horaInicio(), req.horaFin());
        if (req.capacidadMaxima() < reservacionRepository.contarActivas(idEvento)) {
            throw new BusinessException("La capacidad no puede ser menor que las reservaciones activas.");
        }
        aplicar(evento, req, ubicacion);
        EventoResponse response = toResponse(eventoRepository.save(evento));
        notificarReservantes(evento, "El evento '" + evento.getNombre() + "' fue actualizado.");
        return response;
    }

    @Transactional
    public EventoResponse cancelar(Long idEvento, String emailOrganizador) {
        Evento evento = findOwnedEventForUpdate(idEvento, emailOrganizador);
        if ("Cancelado".equalsIgnoreCase(evento.getEstado())) {
            throw new BusinessException("El evento ya está cancelado.");
        }
        if ("Finalizado".equalsIgnoreCase(evento.getEstado())) {
            throw new BusinessException("No se puede cancelar un evento finalizado.");
        }
        evento.setEstado("Cancelado");
        EventoResponse response = toResponse(eventoRepository.save(evento));
        notificarReservantes(evento, "El evento '" + evento.getNombre() + "' fue cancelado.");
        return response;
    }

    @Transactional
    public EventoResponse reprogramar(Long idEvento, ReprogramarEventoRequest req, String emailOrganizador) {
        Evento evento = findOwnedEventForUpdate(idEvento, emailOrganizador);
        validarEventoModificable(evento);
        validarHorario(req.horaInicio(), req.horaFin());
        evento.setFecha(req.fecha());
        evento.setHoraInicio(req.horaInicio());
        evento.setHoraFin(req.horaFin());
        EventoResponse response = toResponse(eventoRepository.save(evento));
        notificarReservantes(evento, "El evento '" + evento.getNombre() + "' fue reprogramado para "
            + evento.getFecha() + " a las " + evento.getHoraInicio() + ".");
        return response;
    }

    @Transactional
    public EventoResponse finalizar(Long idEvento, String emailOrganizador) {
        Evento evento = findOwnedEventForUpdate(idEvento, emailOrganizador);
        validarEventoModificable(evento);
        evento.setEstado("Finalizado");
        EventoResponse response = toResponse(eventoRepository.save(evento));
        notificarReservantes(evento, "El evento '" + evento.getNombre() + "' fue finalizado.");
        return response;
    }

    @Transactional(readOnly = true)
    public List<EventoRecursoResponse> listarRecursos(Long idEvento, String emailOrganizador) {
        findOwnedEvent(idEvento, emailOrganizador);
        return eventoRecursoRepository.findById_IdEvento(idEvento)
            .stream()
            .map(this::toEventoRecursoResponse)
            .toList();
    }

    @Transactional
    public EventoRecursoResponse asignarRecurso(
        Long idEvento,
        EventoRecursoRequest req,
        String emailOrganizador
    ) {
        Evento evento = findOwnedEventForUpdate(idEvento, emailOrganizador);
        validarEventoModificable(evento);
        Recurso recurso = findResourceForUpdate(req.idRecurso());
        EventoRecursoId id = new EventoRecursoId(idEvento, recurso.getIdRecurso());
        if (eventoRecursoRepository.existsById(id)) {
            throw new BusinessException("El recurso ya está asignado al evento.");
        }
        validarDisponibilidadRecurso(recurso, req.cantidadAsignada(), 0);

        EventoRecurso asignacion = new EventoRecurso();
        asignacion.setId(id);
        asignacion.setEvento(evento);
        asignacion.setRecurso(recurso);
        asignacion.setCantidadAsignada(req.cantidadAsignada());
        return toEventoRecursoResponse(eventoRecursoRepository.save(asignacion));
    }

    @Transactional
    public EventoRecursoResponse actualizarRecurso(
        Long idEvento,
        Long idRecurso,
        Integer cantidadAsignada,
        String emailOrganizador
    ) {
        Evento evento = findOwnedEventForUpdate(idEvento, emailOrganizador);
        validarEventoModificable(evento);
        if (cantidadAsignada == null || cantidadAsignada <= 0) {
            throw new BusinessException("La cantidad asignada debe ser mayor que cero.");
        }
        Recurso recurso = findResourceForUpdate(idRecurso);
        EventoRecurso asignacion = findEventoRecurso(idEvento, idRecurso);
        validarDisponibilidadRecurso(recurso, cantidadAsignada, asignacion.getCantidadAsignada());
        asignacion.setCantidadAsignada(cantidadAsignada);
        return toEventoRecursoResponse(eventoRecursoRepository.save(asignacion));
    }

    @Transactional
    public void quitarRecurso(Long idEvento, Long idRecurso, String emailOrganizador) {
        Evento evento = findOwnedEventForUpdate(idEvento, emailOrganizador);
        validarEventoModificable(evento);
        EventoRecurso asignacion = findEventoRecurso(idEvento, idRecurso);
        eventoRecursoRepository.delete(asignacion);
    }

    private void aplicar(Evento evento, EventoRequest req, Ubicacion ubicacion) {
        evento.setNombre(req.nombre().trim());
        evento.setDescripcion(req.descripcion());
        evento.setFecha(req.fecha());
        evento.setHoraInicio(req.horaInicio());
        evento.setHoraFin(req.horaFin());
        evento.setCapacidadMaxima(req.capacidadMaxima());
        evento.setUbicacion(ubicacion);
    }

    private void validarHorario(LocalTime horaInicio, LocalTime horaFin) {
        if (horaFin != null && horaFin.isBefore(horaInicio)) {
            throw new BusinessException("La hora de fin no puede ser anterior a la hora de inicio.");
        }
    }

    private void validarEventoModificable(Evento evento) {
        if ("Cancelado".equalsIgnoreCase(evento.getEstado())) {
            throw new BusinessException("No se puede modificar un evento cancelado.");
        }
        if ("Finalizado".equalsIgnoreCase(evento.getEstado())) {
            throw new BusinessException("El evento ya está finalizado.");
        }
    }

    private void validarDisponibilidadRecurso(Recurso recurso, int nuevaCantidad, int cantidadActual) {
        if (!"Disponible".equalsIgnoreCase(recurso.getEstado())) {
            throw new BusinessException("El recurso no está disponible.");
        }
        long asignadaEnOtrosEventos = eventoRecursoRepository.cantidadAsignada(recurso.getIdRecurso()) - cantidadActual;
        if (asignadaEnOtrosEventos + nuevaCantidad > recurso.getCantidad()) {
            throw new BusinessException("La cantidad solicitada supera las unidades disponibles del recurso.");
        }
    }

    private void notificarReservantes(Evento evento, String mensaje) {
        Map<Long, Reservacion> reservacionesPorUsuario = new LinkedHashMap<>();
        for (Reservacion reservacion : reservacionRepository.findByEvento_IdEventoAndEstadoIn(
            evento.getIdEvento(), RESERVACIONES_ACTIVAS)) {
            reservacionesPorUsuario.putIfAbsent(reservacion.getParticipante().getIdUsuario(), reservacion);
        }
        for (Reservacion reservacion : reservacionesPorUsuario.values()) {
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(reservacion.getParticipante());
            notificacion.setMensaje(mensaje);
            notificacion.setTipo("Evento");
            notificacion.setLeido(false);
            notificacionRepository.saveAndFlush(notificacion);
            notificacionStompService.enviar(notificacion);
        }
    }

    private Evento findOwnedEvent(Long idEvento, String emailOrganizador) {
        Organizador organizador = organizadorRepository.findByEmail(emailOrganizador)
            .orElseThrow(() -> new NotFoundException("Organizador no encontrado: " + emailOrganizador));
        return eventoRepository.findByIdEventoAndOrganizador_IdUsuario(idEvento, organizador.getIdUsuario())
            .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + idEvento));
    }

    private Evento findOwnedEventForUpdate(Long idEvento, String emailOrganizador) {
        Organizador organizador = organizadorRepository.findByEmail(emailOrganizador)
            .orElseThrow(() -> new NotFoundException("Organizador no encontrado: " + emailOrganizador));
        Evento evento = eventoRepository.findByIdForUpdate(idEvento)
            .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + idEvento));
        if (!organizador.getIdUsuario().equals(evento.getOrganizador().getIdUsuario())) {
            throw new NotFoundException("Evento no encontrado: " + idEvento);
        }
        return evento;
    }

    private Evento findEvent(Long idEvento) {
        return eventoRepository.findById(idEvento)
            .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + idEvento));
    }

    private Ubicacion findLocation(Long idUbicacion) {
        return ubicacionRepository.findById(idUbicacion)
            .orElseThrow(() -> new NotFoundException("Ubicación no encontrada: " + idUbicacion));
    }

    private Recurso findResourceForUpdate(Long idRecurso) {
        return recursoRepository.findByIdForUpdate(idRecurso)
            .orElseThrow(() -> new NotFoundException("Recurso no encontrado: " + idRecurso));
    }

    private EventoRecurso findEventoRecurso(Long idEvento, Long idRecurso) {
        EventoRecursoId id = new EventoRecursoId(idEvento, idRecurso);
        return eventoRecursoRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("El recurso no está asignado a este evento."));
    }

    private EventoRecursoResponse toEventoRecursoResponse(EventoRecurso asignacion) {
        return new EventoRecursoResponse(
            asignacion.getEvento().getIdEvento(),
            asignacion.getRecurso().getIdRecurso(),
            asignacion.getRecurso().getNombre(),
            asignacion.getCantidadAsignada());
    }

    private EventoResponse toResponse(Evento evento) {
        long ocupadas = reservacionRepository.contarActivas(evento.getIdEvento());
        long disponibles = cuposDisponibles(evento, ocupadas);
        return new EventoResponse(
            evento.getIdEvento(),
            evento.getNombre(),
            evento.getDescripcion(),
            evento.getFecha(),
            evento.getHoraInicio(),
            evento.getHoraFin(),
            evento.getCapacidadMaxima(),
            evento.getEstado(),
            evento.getUbicacion() != null ? evento.getUbicacion().getIdUbicacion() : null,
            evento.getUbicacion() != null ? evento.getUbicacion().getNombre() : null,
            evento.getOrganizador() != null ? evento.getOrganizador().getIdUsuario() : null,
            evento.getOrganizador() != null ? evento.getOrganizador().getNombre() : null,
            disponibles);
    }

    private long cuposDisponibles(Evento evento, long ocupadas) {
        return "Activo".equalsIgnoreCase(evento.getEstado())
            ? Math.max(0L, evento.getCapacidadMaxima() - ocupadas)
            : 0L;
    }
}
