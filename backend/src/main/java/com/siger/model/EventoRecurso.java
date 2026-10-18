package com.siger.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Join entity for the N:M relation between Evento and Recurso with the
 * extra column cantidadAsignada. The composite key is {@link EventoRecursoId}.
 */
@Entity
@Table(name = "evento_recurso")
@Getter
@Setter
@NoArgsConstructor
public class EventoRecurso {

    @EmbeddedId
    private EventoRecursoId id;

    @MapsId("idEvento")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idEvento")
    private Evento evento;

    @MapsId("idRecurso")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idRecurso")
    private Recurso recurso;

    @Column(name = "cantidadAsignada", nullable = false)
    private Integer cantidadAsignada = 1;
}
