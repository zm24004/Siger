package com.siger.repository;

import com.siger.model.Rol;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, String> {

    @Override
    @EntityGraph(attributePaths = "permisos")
    List<Rol> findAll();

    @Override
    @EntityGraph(attributePaths = "permisos")
    Optional<Rol> findById(String id);
}
