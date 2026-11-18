package com.siger.service;

import com.siger.dto.NotificacionCursor;
import com.siger.dto.NotificacionEnvioRequest;
import com.siger.dto.NotificacionEnvioResponse;
import com.siger.dto.NotificacionPageResponse;
import com.siger.dto.NotificacionResponse;
import com.siger.exception.NotFoundException;
import com.siger.model.Evento;
import com.siger.model.Notificacion;
import com.siger.model.Organizador;
import com.siger.model.Reservacion;
import com.siger.repository.EventoRepository;
import com.siger.repository.NotificacionRepository;
import com.siger.repository.OrganizadorRepository;
import com.siger.repository.ReservacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User-scoped notification operations and organizer/admin reminder delivery.
 */
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final EventoRepository eventoRepository;
    private final OrganizadorRepository organizadorRepository;
    private final ReservacionRepository reservacionRepository;
    private final NotificacionStompService notificacionStompService;

    private static final List<String> RESERVACIONES_ACTIVAS = List.of("Pendiente", "Confirmada", "Pausada");

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorUsuario(Long idUsuario) {
        return notificacionRepository
            .findByUsuario_IdUsuarioOrderByLeidoAscFechaEnvioDesc(idUsuario)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public NotificacionPageResponse listarPaginaPorUsuario(
        Long idUsuario,
        NotificacionCursor cursor,
        int limit
    ) {
        List<Notificacion> resultados = notificacionRepository.buscarPagina(
            idUsuario,
            cursor == null ? null : cursor.fechaEnvio(),
            cursor == null ? null : cursor.idNotificacion(),
            PageRequest.of(0, limit + 1));

        boolean hasMore = resultados.size() > limit;
        List<Notificacion> pagina = hasMore
            ? resultados.subList(0, limit)
            : resultados;
        List<NotificacionResponse> content = pagina.stream().map(this::toResponse).toList();
        NotificacionCursor nextCursor = hasMore && !pagina.isEmpty()
            ? new NotificacionCursor(
                pagina.getLast().getFechaEnvio(),
                pagina.getLast().getIdNotificacion())
            : null;

        return new NotificacionPageResponse(content, nextCursor, hasMore);
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(Long idUsuario) {
        var count = notificacionRepository.countByUsuario_IdUsuarioAndLeidoFalse(idUsuario);
        return count;
    }

    @Transactional(readOnly = true)
    public NotificacionResponse obtener(Long idNotificacion, Long idUsuario) {
        return toResponse(findOwned(idNotificacion, idUsuario));
    }

    @Transactional
    public NotificacionResponse marcarLeida(Long idNotificacion, Long idUsuario) {
        Notificacion n = findOwned(idNotificacion, idUsuario);
        n.setLeido(true);
        return toResponse(notificacionRepository.save(n));
    }

    @Transactional
    public void eliminar(Long idNotificacion, Long idUsuario) {
        notificacionRepository.delete(findOwned(idNotificacion, idUsuario));
    }

    @Transactional
    public NotificacionEnvioResponse enviarRecordatorio(
        NotificacionEnvioRequest request,
        String emailSolicitante,
        boolean administrador
    ) {
        Evento evento = eventoRepository.findById(request.idEvento())
            .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + request.idEvento()));
        if (!administrador) {
            Organizador organizador = organizadorRepository.findByEmail(emailSolicitante)
                .orElseThrow(() -> new NotFoundException("Organizador no encontrado: " + emailSolicitante));
            if (!organizador.getIdUsuario().equals(evento.getOrganizador().getIdUsuario())) {
                throw new NotFoundException("Evento no encontrado: " + request.idEvento());
            }
        }

        String mensaje = request.mensaje() == null || request.mensaje().isBlank()
            ? "Recordatorio: tienes una reservación para el evento '" + evento.getNombre()
                + "' el " + evento.getFecha() + "."
            : request.mensaje().trim();

        List<Reservacion> reservaciones = reservacionRepository.findByEvento_IdEventoAndEstadoIn(
            evento.getIdEvento(), RESERVACIONES_ACTIVAS);
        for (Reservacion reservacion : reservaciones) {
            Notificacion notificacion = new Notificacion();
            notificacion.setMensaje(mensaje);
            notificacion.setTipo("Recordatorio");
            notificacion.setLeido(false);
            notificacion.setUsuario(reservacion.getParticipante());
            notificacionStompService.enviar(notificacionRepository.save(notificacion));
        }
        return new NotificacionEnvioResponse(evento.getIdEvento(), reservaciones.size());
    }

    private Notificacion findOwned(Long idNotificacion, Long idUsuario) {
        return notificacionRepository.findByIdNotificacionAndUsuario_IdUsuario(idNotificacion, idUsuario)
            .orElseThrow(() -> new NotFoundException("Notificación no encontrada: " + idNotificacion));
    }

    private NotificacionResponse toResponse(Notificacion n) {
        return new NotificacionResponse(
            n.getIdNotificacion(),
            n.getMensaje(),
            n.getTipo(),
            n.getLeido(),
            n.getFechaEnvio()
        );
    }
}
