package com.siger.repository;

import com.siger.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    boolean existsByReservacion_IdReservacion(Long idReservacion);

    List<Asistencia> findByReservacion_Participante_IdUsuario(Long idParticipante);
}
