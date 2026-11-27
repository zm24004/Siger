package com.siger.repository;

import com.siger.model.Recurso;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecursoRepository extends JpaRepository<Recurso, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Recurso r where r.idRecurso = :id")
    Optional<Recurso> findByIdForUpdate(@Param("id") Long id);
}
