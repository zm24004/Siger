package com.siger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "reservacion")
@Getter
@Setter
@NoArgsConstructor
public class Reservacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReservacion")
    private Long idReservacion;

    @Column(name = "fechaReservacion", nullable = false)
    private LocalDate fechaReservacion = LocalDate.now();

    @Column(name = "codigoReservacion", nullable = false, unique = true, length = 20)
    private String codigoReservacion;

    @Column(nullable = false, length = 20)
    private String estado = "Pendiente";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idEvento", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idParticipante", nullable = false)
    private Participante participante;
}
