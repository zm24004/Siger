package com.siger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permiso")
@Getter
@Setter
@NoArgsConstructor
public class Permiso {

    @Id
    @Column(name = "idPermiso", length = 80)
    private String idPermiso;

    @Column(nullable = false, length = 150)
    private String descripcion;

    public Permiso(String idPermiso, String descripcion) {
        this.idPermiso = idPermiso;
        this.descripcion = descripcion;
    }
}
