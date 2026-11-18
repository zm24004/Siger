package com.siger.repository;

import com.siger.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findAllByOrderByFechaGeneracionDesc();

    List<Reporte> findByCreador_IdUsuarioOrderByFechaGeneracionDesc(Long idUsuario);

    Optional<Reporte> findByIdReporteAndCreador_IdUsuario(Long idReporte, Long idUsuario);
}
