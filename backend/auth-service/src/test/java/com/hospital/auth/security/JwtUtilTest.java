package com.hospital.auth.security;

import com.hospital.auth.entity.Role;
import com.hospital.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("test-secret-test-secret-test-secret-test-secret-1234567890".getBytes());

    @Test
    void generates_token_with_expected_claims() {
        JwtUtil util = new JwtUtil(SECRET, 60_000L);
        User user = User.builder().id(7L).email("alice@example.com").role(Role.DOCTOR).password("x").build();

        String token = util.generate(user);

        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        assertThat(claims.getSubject()).isEqualTo("alice@example.com");
        assertThat(claims.get("email", String.class)).isEqualTo("alice@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("DOCTOR");
        assertThat(claims.get("userId", Number.class).longValue()).isEqualTo(7L);
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }
}
