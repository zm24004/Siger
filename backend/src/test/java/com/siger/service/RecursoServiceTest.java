package com.siger.service;

import com.siger.dto.PageResponse;
import com.siger.dto.RecursoRequest;
import com.siger.dto.RecursoResponse;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Recurso;
import com.siger.repository.EventoRecursoRepository;
import com.siger.repository.RecursoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecursoServiceTest {

    @Mock RecursoRepository recursoRepository;
    @Mock EventoRecursoRepository eventoRecursoRepository;
    @InjectMocks RecursoService recursoService;

    @Test
    void listar_devuelveContenidoYMetadatosDePagina() {
        Recurso recurso = recurso(9L, "Proyector", 2);
        when(recursoRepository.findAll(PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(List.of(recurso), PageRequest.of(0, 20), 1));

        PageResponse<RecursoResponse> result = recursoService.listar(1, 20);

        assertThat(result.content()).singleElement()
            .extracting(RecursoResponse::idRecurso).isEqualTo(9L);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.limit()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void actualizar_actualizaLosCamposDelRecurso() {
        Recurso recurso = recurso(9L, "Proyector", 1);
        when(recursoRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(recurso));
        when(eventoRecursoRepository.cantidadAsignada(9L)).thenReturn(0L);
        when(recursoRepository.save(recurso)).thenReturn(recurso);

        RecursoResponse result = recursoService.actualizar(9L,
            new RecursoRequest("Pantalla", "HD", 3, "Visual", "Disponible"));

        assertThat(result.nombre()).isEqualTo("Pantalla");
        assertThat(result.descripcion()).isEqualTo("HD");
        assertThat(result.cantidad()).isEqualTo(3);
        assertThat(result.tipo()).isEqualTo("Visual");
    }

    @Test
    void detalle_inexistente_lanzaNotFound() {
        when(recursoRepository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recursoService.detalle(44L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("44");
    }

    @Test
    void eliminar_asignadoLanzaConflictoYNoBorra() {
        Recurso recurso = recurso(9L, "Proyector", 1);
        when(recursoRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(recurso));
        when(eventoRecursoRepository.existsByRecurso_IdRecurso(9L)).thenReturn(true);

        assertThatThrownBy(() -> recursoService.eliminar(9L))
            .isInstanceOf(BusinessException.class);
        verify(recursoRepository, never()).delete(any(Recurso.class));
    }

    @Test
    void eliminar_sinAsignaciones_borraElRecurso() {
        Recurso recurso = recurso(9L, "Proyector", 1);
        when(recursoRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(recurso));
        when(eventoRecursoRepository.existsByRecurso_IdRecurso(9L)).thenReturn(false);

        recursoService.eliminar(9L);

        verify(recursoRepository).delete(recurso);
    }

    private static Recurso recurso(Long id, String nombre, Integer cantidad) {
        Recurso recurso = new Recurso();
        recurso.setIdRecurso(id);
        recurso.setNombre(nombre);
        recurso.setCantidad(cantidad);
        recurso.setTipo("General");
        recurso.setEstado("Disponible");
        return recurso;
    }
}
