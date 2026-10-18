package com.siger.service;

import com.siger.model.Administrador;
import com.siger.model.Organizador;
import com.siger.model.Participante;
import com.siger.model.Usuario;

/**
 * Derives the SIGER role string from a Usuario's concrete subtype.
 */
public final class RolHelper {

    private RolHelper() {}

    public static String derivarRol(Usuario u) {
        if (u instanceof Administrador) return "ADMINISTRADOR";
        if (u instanceof Organizador)   return "ORGANIZADOR";
        if (u instanceof Participante) return "PARTICIPANTE";
        throw new IllegalStateException("Tipo de usuario no soportado: " + u.getClass().getName());
    }

    /**
     * Generates a unique human-readable reservation code: "SIG-XXXXXXXX"
     * (8 chars from a UUID, uppercased).
     */
    public static String generarCodigoReservacion() {
        return "SIG-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
