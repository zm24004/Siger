package com.siger.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for the EventoRecurso join entity.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoRecursoId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idEvento;
    private Long idRecurso;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventoRecursoId that)) return false;
        return Objects.equals(idEvento, that.idEvento) && Objects.equals(idRecurso, that.idRecurso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEvento, idRecurso);
    }
}
