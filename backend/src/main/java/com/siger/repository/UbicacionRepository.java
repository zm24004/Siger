package com.siger.repository;

import com.siger.model.Ubicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    Page<Ubicacion> findByTipoIgnoreCase(String tipo, Pageable pageable);
}
