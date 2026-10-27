package com.siger.service;

import com.siger.dto.PageResponse;
import com.siger.dto.UbicacionRequest;
import com.siger.dto.UbicacionResponse;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Ubicacion;
import com.siger.repository.EventoRepository;
import com.siger.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD for Ubicacion.
 */
@Service
@RequiredArgsConstructor
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final EventoRepository eventoRepository;

    @Transactional(readOnly = true)
    public PageResponse<UbicacionResponse> listar(String tipo, int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit);
        Page<Ubicacion> ubicaciones = tipo == null || tipo.isBlank()
            ? ubicacionRepository.findAll(pageable)
            : ubicacionRepository.findByTipoIgnoreCase(tipo.trim(), pageable);
        return new PageResponse<>(
            ubicaciones.getContent().stream().map(this::toResponse).toList(),
            page,
            limit,
            ubicaciones.getTotalElements(),
            ubicaciones.getTotalPages());
    }

    @Transactional(readOnly = true)
    public UbicacionResponse detalle(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public UbicacionResponse crear(UbicacionRequest req) {
        Ubicacion u = new Ubicacion();
        aplicar(u, req);
        return toResponse(ubicacionRepository.save(u));
    }

    @Transactional
    public UbicacionResponse actualizar(Long id, UbicacionRequest req) {
        Ubicacion ubicacion = findById(id);
        aplicar(ubicacion, req);
        return toResponse(ubicacionRepository.save(ubicacion));
    }

    @Transactional
    public void eliminar(Long id) {
        Ubicacion ubicacion = findById(id);
        if (eventoRepository.existsByUbicacion_IdUbicacion(id)) {
            throw new BusinessException("No se puede eliminar la ubicación porque está asignada a uno o más eventos.");
        }
        ubicacionRepository.delete(ubicacion);
    }

    private void aplicar(Ubicacion ubicacion, UbicacionRequest req) {
        ubicacion.setNombre(req.nombre().trim());
        ubicacion.setDireccion(req.direccion());
        ubicacion.setTipo(req.tipo() == null || req.tipo().isBlank() ? "Presencial" : req.tipo().trim());
        ubicacion.setEnlaceVirtual(req.enlaceVirtual());
    }

    private Ubicacion findById(Long id) {
        return ubicacionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Ubicación no encontrada: " + id));
    }

    private UbicacionResponse toResponse(Ubicacion u) {
        return new UbicacionResponse(
            u.getIdUbicacion(),
            u.getNombre(),
            u.getDireccion(),
            u.getTipo(),
            u.getEnlaceVirtual()
        );
    }
}
