package com.siger.repository;

import com.siger.model.Reservacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservacionRepository extends JpaRepository<Reservacion, Long> {

    boolean existsByEvento_IdEventoAndParticipante_IdUsuario(Long idEvento, Long idParticipante);

    Optional<Reservacion> findByCodigoReservacion(String codigoReservacion);

    List<Reservacion> findByParticipante_IdUsuario(Long idParticipante);

    // Cuenta reservas activas (Pendiente + Confirmada + Pausada).
    // Las pausadas siguen ocupando cupo; solo Cancelada libera.
    @Query("select count(r) from Reservacion r where r.evento.idEvento = :id and r.estado in ('Pendiente','Confirmada','Pausada')")
    long contarActivas(@Param("id") Long idEvento);
}
