package com.siger.repository;

import com.siger.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    boolean existsByReservacion_IdReservacion(Long idReservacion);

    List<Asistencia> findByReservacion_Participante_IdUsuario(Long idParticipante);

    List<Asistencia> findByReservacion_Evento_IdEventoOrderByFechaAsistenciaAscHoraAsistenciaAsc(
        Long idEvento);

    Optional<Asistencia> findByIdAsistenciaAndReservacion_Evento_Organizador_IdUsuario(
        Long idAsistencia, Long idOrganizador);
}
