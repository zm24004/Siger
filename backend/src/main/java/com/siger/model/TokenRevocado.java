package com.siger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "jwt_revocado")
@Getter
@Setter
@NoArgsConstructor
public class TokenRevocado {

    @Id
    @Column(name = "hashToken", length = 64)
    private String hashToken;

    @Column(name = "expiraEn", nullable = false)
    private Instant expiraEn;

    public TokenRevocado(String hashToken, Instant expiraEn) {
        this.hashToken = hashToken;
        this.expiraEn = expiraEn;
    }
}
