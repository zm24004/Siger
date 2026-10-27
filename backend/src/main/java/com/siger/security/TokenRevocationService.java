package com.siger.security;

import com.siger.model.TokenRevocado;
import com.siger.repository.TokenRevocadoRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final TokenRevocadoRepository tokenRevocadoRepository;
    private final JwtService jwtService;

    @Transactional
    public void revocar(String token) {
        Claims claims = jwtService.leer(token);
        String hash = hash(token);
        if (!tokenRevocadoRepository.existsById(hash)) {
            tokenRevocadoRepository.save(new TokenRevocado(hash, claims.getExpiration().toInstant()));
        }
    }

    @Transactional(readOnly = true)
    public boolean estaRevocado(String token) {
        return tokenRevocadoRepository.existsById(hash(token));
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible en esta JVM.", ex);
        }
    }
}
