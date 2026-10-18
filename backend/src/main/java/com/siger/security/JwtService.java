package com.siger.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT generation and parsing service built on jjwt 0.12.6 API.
 */
@Service
public class JwtService {

    @Value("${siger.jwt.secret}")
    private String secret;

    @Value("${siger.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey key;

    @PostConstruct
    void init() {
        // Allow either plain-text (>= 32 chars) or Base64-encoded secrets.
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
            if (keyBytes.length < 32) {
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            }
        } catch (RuntimeException ex) {
            // IllegalArgumentException (bad base64 length) o DecodingException
            // (caracteres no base64 como '-' o '_') → fallback a UTF-8 crudo.
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "siger.jwt.secret must be at least 32 bytes for HS256. " +
                "Set JWT_SECRET to a long random string.");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT for the given subject (email) and rol claim.
     */
    public String generar(String email, String rol) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
            .subject(email)
            .claim("rol", rol)
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact();
    }

    /**
     * Parses and verifies a JWT, returning the claims payload.
     *
     * @throws JwtException if the token is invalid or expired.
     */
    public Claims leer(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
