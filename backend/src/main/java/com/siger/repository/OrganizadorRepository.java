package com.siger.repository;

import com.siger.model.Organizador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizadorRepository extends JpaRepository<Organizador, Long> {

    Optional<Organizador> findByEmail(String email);
}
