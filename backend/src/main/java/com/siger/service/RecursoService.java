package com.siger.service;

import com.siger.dto.RecursoRequest;
import com.siger.dto.RecursoResponse;
import com.siger.exception.NotFoundException;
import com.siger.model.Recurso;
import com.siger.repository.RecursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for Recurso (organizador-only).
 */
@Service
@RequiredArgsConstructor
public class RecursoService {

    private final RecursoRepository recursoRepository;

    @Transactional(readOnly = true)
    public List<RecursoResponse> listar() {
        return recursoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public RecursoResponse crear(RecursoRequest req) {
        Recurso r = new Recurso();
        r.setNombre(req.nombre());
        r.setDescripcion(req.descripcion());
        r.setCantidad(req.cantidad());
        r.setTipo(req.tipo() == null || req.tipo().isBlank() ? "General" : req.tipo());
        r.setEstado(req.estado() == null || req.estado().isBlank() ? "Disponible" : req.estado());
        return toResponse(recursoRepository.save(r));
    }

    private RecursoResponse toResponse(Recurso r) {
        return new RecursoResponse(
            r.getIdRecurso(),
            r.getNombre(),
            r.getDescripcion(),
            r.getCantidad(),
            r.getTipo(),
            r.getEstado()
        );
    }
}
