package com.siger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "asistencia")
@Getter
@Setter
@NoArgsConstructor
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAsistencia")
    private Long idAsistencia;

    @Column(name = "fechaAsistencia", nullable = false)
    private LocalDate fechaAsistencia = LocalDate.now();

    @Column(name = "horaAsistencia", nullable = false)
    private LocalTime horaAsistencia = LocalTime.now();

    @Column(nullable = false, length = 20)
    private String estado = "Presente";

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idReservacion", nullable = false, unique = true)
    private Reservacion reservacion;
}
