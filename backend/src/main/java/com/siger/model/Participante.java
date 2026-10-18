package com.siger.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "participante")
@PrimaryKeyJoinColumn(name = "idUsuario")
@Getter
@Setter
@NoArgsConstructor
public class Participante extends Usuario {
}
