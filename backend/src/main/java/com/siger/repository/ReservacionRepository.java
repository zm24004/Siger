package com.siger.repository;

import com.siger.model.Reservacion;
import com.siger.dto.ReservacionesPorEstadoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservacionRepository extends JpaRepository<Reservacion, Long> {

    boolean existsByEvento_IdEventoAndParticipante_IdUsuario(Long idEvento, Long idParticipante);

    Optional<Reservacion> findByCodigoReservacion(String codigoReservacion);

    List<Reservacion> findByParticipante_IdUsuario(Long idParticipante);

    List<Reservacion> findByEvento_IdEventoOrderByFechaReservacionAsc(Long idEvento);

    List<Reservacion> findByParticipante_IdUsuarioOrderByFechaReservacionDesc(Long idParticipante);

    List<Reservacion> findByEvento_IdEventoAndEstadoIn(
        Long idEvento,
        List<String> estados);

    @Query("""
        select r from Reservacion r
        where (:idEvento is null or r.evento.idEvento = :idEvento)
          and (:estado is null or lower(r.estado) = lower(:estado))
          and (:idOrganizador is null or r.evento.organizador.idUsuario = :idOrganizador)
        order by r.fechaReservacion desc, r.idReservacion desc
        """)
    List<Reservacion> buscar(
        @Param("idEvento") Long idEvento,
        @Param("estado") String estado,
        @Param("idOrganizador") Long idOrganizador);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservacion r where r.codigoReservacion = :codigo")
    Optional<Reservacion> findByCodigoReservacionForUpdate(@Param("codigo") String codigo);

    // Cuenta reservas activas (Pendiente + Confirmada + Pausada).
    // Las pausadas siguen ocupando cupo; solo Cancelada libera.
    @Query("select count(r) from Reservacion r where r.evento.idEvento = :id and r.estado in ('Pendiente','Confirmada','Pausada')")
    long contarActivas(@Param("id") Long idEvento);

    @Query("""
        select new com.siger.dto.ReservacionesPorEstadoResponse(r.estado, count(r.idReservacion))
        from Reservacion r
        where (:idEvento is null or r.evento.idEvento = :idEvento)
          and (:idOrganizador is null or r.evento.organizador.idUsuario = :idOrganizador)
          and (:fechaDesde is null or r.evento.fecha >= :fechaDesde)
          and (:fechaHasta is null or r.evento.fecha <= :fechaHasta)
        group by r.estado
        order by r.estado
        """)
    List<ReservacionesPorEstadoResponse> contarPorEstadoParaReporte(
        @Param("idEvento") Long idEvento,
        @Param("idOrganizador") Long idOrganizador,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta);
}
