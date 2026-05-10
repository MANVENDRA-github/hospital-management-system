package com.hospital.auth.security;

import com.hospital.auth.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(@Value("${app.jwt.secret}") String base64Secret,
                   @Value("${app.jwt.expiration}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.expirationMs = expirationMs;
    }

    public String generate(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claims(Map.of(
                        "userId", user.getId(),
                        "email", user.getEmail(),
                        "role", user.getRole().name()))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }
}
