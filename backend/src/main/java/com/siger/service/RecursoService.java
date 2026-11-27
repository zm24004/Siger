package com.siger.service;

import com.siger.dto.PageResponse;
import com.siger.dto.RecursoRequest;
import com.siger.dto.RecursoResponse;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Recurso;
import com.siger.repository.EventoRecursoRepository;
import com.siger.repository.RecursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD for Recurso.
 */
@Service
@RequiredArgsConstructor
public class RecursoService {

    private final RecursoRepository recursoRepository;
    private final EventoRecursoRepository eventoRecursoRepository;

    @Transactional(readOnly = true)
    public PageResponse<RecursoResponse> listar(int page, int limit) {
        Page<Recurso> recursos = recursoRepository.findAll(PageRequest.of(page - 1, limit));
        return new PageResponse<>(
            recursos.getContent().stream().map(this::toResponse).toList(),
            page,
            limit,
            recursos.getTotalElements(),
            recursos.getTotalPages());
    }

    @Transactional(readOnly = true)
    public RecursoResponse detalle(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public RecursoResponse crear(RecursoRequest req) {
        Recurso r = new Recurso();
        aplicar(r, req);
        return toResponse(recursoRepository.save(r));
    }

    @Transactional
    public RecursoResponse actualizar(Long id, RecursoRequest req) {
        Recurso recurso = recursoRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new NotFoundException("Recurso no encontrado: " + id));
        if (req.cantidad() < eventoRecursoRepository.cantidadAsignada(id)) {
            throw new BusinessException("La cantidad no puede ser menor que las unidades asignadas a eventos activos.");
        }
        aplicar(recurso, req);
        return toResponse(recursoRepository.save(recurso));
    }

    @Transactional
    public void eliminar(Long id) {
        Recurso recurso = recursoRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new NotFoundException("Recurso no encontrado: " + id));
        if (eventoRecursoRepository.existsByRecurso_IdRecurso(id)) {
            throw new BusinessException("No se puede eliminar el recurso porque está asignado a uno o más eventos.");
        }
        recursoRepository.delete(recurso);
    }

    private void aplicar(Recurso recurso, RecursoRequest req) {
        recurso.setNombre(req.nombre().trim());
        recurso.setDescripcion(req.descripcion());
        recurso.setCantidad(req.cantidad());
        recurso.setTipo(req.tipo() == null || req.tipo().isBlank() ? "General" : req.tipo().trim());
        recurso.setEstado(req.estado() == null || req.estado().isBlank() ? "Disponible" : req.estado().trim());
    }

    private Recurso findById(Long id) {
        return recursoRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Recurso no encontrado: " + id));
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
