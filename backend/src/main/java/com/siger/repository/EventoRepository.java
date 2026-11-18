package com.siger.repository;

import com.siger.model.Evento;
import com.siger.dto.EstadisticaEventoResponse;
import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByEstadoAndFechaGreaterThanEqualOrderByFechaAsc(String estado, LocalDate fecha);

    List<Evento> findByOrganizador_IdUsuario(Long idOrganizador);

    Optional<Evento> findByIdEventoAndOrganizador_IdUsuario(Long idEvento, Long idOrganizador);

    @Query(value = """
        select e from Evento e
        where (:estado is null or lower(e.estado) = lower(:estado))
          and (:fechaDesde is null or e.fecha >= :fechaDesde)
          and (:fechaHasta is null or e.fecha <= :fechaHasta)
          and (:idUbicacion is null or e.ubicacion.idUbicacion = :idUbicacion)
        order by e.fecha asc, e.horaInicio asc
        """, countQuery = """
        select count(e) from Evento e
        where (:estado is null or lower(e.estado) = lower(:estado))
          and (:fechaDesde is null or e.fecha >= :fechaDesde)
          and (:fechaHasta is null or e.fecha <= :fechaHasta)
          and (:idUbicacion is null or e.ubicacion.idUbicacion = :idUbicacion)
        """)
    Page<Evento> buscarCatalogo(
        @Param("estado") String estado,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta,
        @Param("idUbicacion") Long idUbicacion,
        Pageable pageable);

    boolean existsByUbicacion_IdUbicacion(Long idUbicacion);

    @Query("""
        select new com.siger.dto.EstadisticaEventoResponse(
            e.idEvento, e.nombre, e.fecha, e.capacidadMaxima,
            count(distinct r.idReservacion),
            coalesce(sum(case when lower(a.estado) = 'presente' then 1 else 0 end), 0))
        from Evento e
        left join Reservacion r on r.evento.idEvento = e.idEvento
            and r.estado in ('Pendiente', 'Confirmada', 'Pausada')
        left join Asistencia a on a.reservacion.idReservacion = r.idReservacion
        where (:idOrganizador is null or e.organizador.idUsuario = :idOrganizador)
          and (:fechaDesde is null or e.fecha >= :fechaDesde)
          and (:fechaHasta is null or e.fecha <= :fechaHasta)
          and (:idEvento is null or e.idEvento = :idEvento)
        group by e.idEvento, e.nombre, e.fecha, e.capacidadMaxima
        order by e.fecha asc, e.idEvento asc
        """)
    List<EstadisticaEventoResponse> obtenerParticipacion(
        @Param("idOrganizador") Long idOrganizador,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta,
        @Param("idEvento") Long idEvento);

    /**
     * Loads an event with a pessimistic write lock for the reservation flow.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Evento e where e.idEvento = :id")
    Optional<Evento> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select e from Evento e
        left join Reservacion r on r.evento.idEvento = e.idEvento and lower(r.estado) <> 'cancelado'
        left join Ubicacion u on u.idUbicacion = e.ubicacion.idUbicacion
        where lower(e.estado) = 'activo'
        group by e
        order by count(r.idReservacion) desc, e.fecha asc
        """)
    Page<Evento> findPopularActiveEvents(Pageable pageable);


    Page<Evento> findByEstado(String estado, Pageable pageable);
}
