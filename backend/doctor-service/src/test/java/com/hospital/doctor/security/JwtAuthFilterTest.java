package com.hospital.doctor.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthFilterTest {

    private static final String SECRET_B64 = Base64.getEncoder()
            .encodeToString("test-secret-test-secret-test-secret-test-secret-1234567890".getBytes());

    private final JwtAuthFilter filter = new JwtAuthFilter(SECRET_B64);

    @AfterEach
    void clear() { SecurityContextHolder.clearContext(); }

    private String validToken() {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_B64));
        return Jwts.builder()
                .subject("doc@x.com")
                .claims(Map.of("role", "DOCTOR"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    @Test
    void valid_token_authenticates() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/doctors");
        req.addHeader("Authorization", "Bearer " + validToken());
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(chain).doFilter(req, null);
    }

    @Test
    void missing_authorization_header_skips() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(new MockHttpServletRequest("GET", "/api/doctors"), new MockHttpServletResponse(), chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void bad_token_clears_context() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/doctors");
        req.addHeader("Authorization", "Bearer garbage");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, new MockHttpServletResponse(), chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
