package com.hospital.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET_B64 = Base64.getEncoder()
            .encodeToString("test-secret-test-secret-test-secret-test-secret-1234567890".getBytes());

    private final JwtUtil util = new JwtUtil(SECRET_B64);

    private String validToken() {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_B64));
        return Jwts.builder().subject("u@x.com").claims(Map.of("role", "ADMIN"))
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 60000)).signWith(key).compact();
    }

    private String expiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_B64));
        return Jwts.builder().subject("u@x.com")
                .issuedAt(new Date(System.currentTimeMillis() - 120000))
                .expiration(new Date(System.currentTimeMillis() - 60000))
                .signWith(key).compact();
    }

    @Test
    void parseClaims_returns_claims_for_valid_token() {
        Claims claims = util.parseClaims(validToken());
        assertThat(claims.getSubject()).isEqualTo("u@x.com");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void isValid_returns_true_for_valid_token() {
        assertThat(util.isValid(validToken())).isTrue();
    }

    @Test
    void isValid_returns_false_for_expired_token() {
        assertThat(util.isValid(expiredToken())).isFalse();
    }

    @Test
    void isValid_returns_false_for_garbage() {
        assertThat(util.isValid("garbage")).isFalse();
    }
}
