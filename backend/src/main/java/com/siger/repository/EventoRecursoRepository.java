package com.siger.repository;

import com.siger.model.EventoRecurso;
import com.siger.model.EventoRecursoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoRecursoRepository extends JpaRepository<EventoRecurso, EventoRecursoId> {

    List<EventoRecurso> findById_IdEvento(Long idEvento);
}
