package com.siger.repository;

import com.siger.model.Evento;
import jakarta.persistence.LockModeType;
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

    /**
     * Loads an event with a pessimistic write lock for the reservation flow.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Evento e where e.idEvento = :id")
    Optional<Evento> findByIdForUpdate(@Param("id") Long id);
}
