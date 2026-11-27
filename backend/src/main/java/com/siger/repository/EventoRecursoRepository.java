package com.siger.repository;

import com.siger.model.EventoRecurso;
import com.siger.model.EventoRecursoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventoRecursoRepository extends JpaRepository<EventoRecurso, EventoRecursoId> {

    List<EventoRecurso> findById_IdEvento(Long idEvento);

    boolean existsByRecurso_IdRecurso(Long idRecurso);

    @Query("""
        select coalesce(sum(er.cantidadAsignada), 0)
        from EventoRecurso er
        where er.recurso.idRecurso = :idRecurso and lower(er.evento.estado) = 'activo'
        """)
    long cantidadAsignada(@Param("idRecurso") Long idRecurso);
}
