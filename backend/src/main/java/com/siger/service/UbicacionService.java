package com.siger.service;

import com.siger.dto.UbicacionRequest;
import com.siger.dto.UbicacionResponse;
import com.siger.exception.NotFoundException;
import com.siger.model.Ubicacion;
import com.siger.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for Ubicacion. Listing is public; creation requires ORGANIZADOR.
 */
@Service
@RequiredArgsConstructor
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;

    @Transactional(readOnly = true)
    public List<UbicacionResponse> listar() {
        return ubicacionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UbicacionResponse crear(UbicacionRequest req) {
        Ubicacion u = new Ubicacion();
        u.setNombre(req.nombre());
        u.setDireccion(req.direccion());
        u.setTipo(req.tipo() == null || req.tipo().isBlank() ? "Presencial" : req.tipo());
        u.setEnlaceVirtual(req.enlaceVirtual());
        return toResponse(ubicacionRepository.save(u));
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
