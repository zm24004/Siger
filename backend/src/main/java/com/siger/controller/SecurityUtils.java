package com.siger.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Small utility to access the authenticated principal's email (the JWT subject).
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the username (email) of the currently authenticated user, or null.
     */
    public static String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        return principal != null ? principal.toString() : null;
    }

    /**
     * Returns the idUsuario (parsed from the principal name) — here we just
     * delegate to {@link #currentEmail()} since the JWT subject is the email.
     */
    public static String requireEmail() {
        String email = currentEmail();
        if (email == null) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto de seguridad.");
        }
        return email;
    }
}
