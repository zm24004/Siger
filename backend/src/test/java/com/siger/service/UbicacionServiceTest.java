package com.siger.service;

import com.siger.dto.PageResponse;
import com.siger.dto.UbicacionRequest;
import com.siger.dto.UbicacionResponse;
import com.siger.exception.BusinessException;
import com.siger.exception.NotFoundException;
import com.siger.model.Ubicacion;
import com.siger.repository.EventoRepository;
import com.siger.repository.UbicacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UbicacionServiceTest {

    @Mock UbicacionRepository ubicacionRepository;
    @Mock EventoRepository eventoRepository;
    @InjectMocks UbicacionService ubicacionService;

    @Test
    void listar_filtraTipoYDevuelveMetadatosDePagina() {
        Ubicacion ubicacion = ubicacion(3L, "Auditorio", "Presencial");
        when(ubicacionRepository.findByTipoIgnoreCase(any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(ubicacion), org.springframework.data.domain.PageRequest.of(1, 5), 6));

        PageResponse<UbicacionResponse> result = ubicacionService.listar("  PRESENCIAL  ", 2, 5);

        assertThat(result.content()).singleElement()
            .extracting(UbicacionResponse::idUbicacion).isEqualTo(3L);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.limit()).isEqualTo(5);
        assertThat(result.totalElements()).isEqualTo(6);
        assertThat(result.totalPages()).isEqualTo(2);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(ubicacionRepository).findByTipoIgnoreCase(org.mockito.ArgumentMatchers.eq("PRESENCIAL"), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void crear_aplicaValoresPredeterminados() {
        when(ubicacionRepository.save(any(Ubicacion.class))).thenAnswer(invocation -> {
            Ubicacion saved = invocation.getArgument(0);
            saved.setIdUbicacion(7L);
            return saved;
        });

        UbicacionResponse result = ubicacionService.crear(new UbicacionRequest("  Sala A  ", "Edificio 1", null, null));

        assertThat(result.idUbicacion()).isEqualTo(7L);
        assertThat(result.nombre()).isEqualTo("Sala A");
        assertThat(result.tipo()).isEqualTo("Presencial");
    }

    @Test
    void detalle_inexistente_lanzaNotFound() {
        when(ubicacionRepository.findById(22L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ubicacionService.detalle(22L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("22");
    }

    @Test
    void eliminar_enUso_lanzaConflictoYNoBorra() {
        Ubicacion ubicacion = ubicacion(3L, "Auditorio", "Presencial");
        when(ubicacionRepository.findById(3L)).thenReturn(Optional.of(ubicacion));
        when(eventoRepository.existsByUbicacion_IdUbicacion(3L)).thenReturn(true);

        assertThatThrownBy(() -> ubicacionService.eliminar(3L))
            .isInstanceOf(BusinessException.class);
        verify(ubicacionRepository, never()).delete(any(Ubicacion.class));
    }

    @Test
    void eliminar_sinReferencias_borraLaUbicacion() {
        Ubicacion ubicacion = ubicacion(3L, "Auditorio", "Presencial");
        when(ubicacionRepository.findById(3L)).thenReturn(Optional.of(ubicacion));
        when(eventoRepository.existsByUbicacion_IdUbicacion(3L)).thenReturn(false);

        ubicacionService.eliminar(3L);

        verify(ubicacionRepository).delete(ubicacion);
    }

    private static Ubicacion ubicacion(Long id, String nombre, String tipo) {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setIdUbicacion(id);
        ubicacion.setNombre(nombre);
        ubicacion.setTipo(tipo);
        return ubicacion;
    }
}
