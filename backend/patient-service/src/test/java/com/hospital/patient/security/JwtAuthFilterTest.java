package com.hospital.patient.security;

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

    private static final String SECRET_PLAIN = "test-secret-test-secret-test-secret-test-secret-1234567890";
    private static final String SECRET_B64 = Base64.getEncoder().encodeToString(SECRET_PLAIN.getBytes());

    private final JwtAuthFilter filter = new JwtAuthFilter(SECRET_B64);

    @AfterEach
    void clear() { SecurityContextHolder.clearContext(); }

    private String validToken(String role) {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_B64));
        return Jwts.builder()
                .subject("u@x.com")
                .claims(Map.of("role", role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    @Test
    void valid_token_sets_authentication() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/patients");
        req.addHeader("Authorization", "Bearer " + validToken("ADMIN"));
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        verify(chain).doFilter(req, res);
    }

    @Test
    void missing_header_skips_authentication() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/patients");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }

    @Test
    void malformed_token_clears_context_but_continues_chain() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/patients");
        req.addHeader("Authorization", "Bearer not-a-jwt");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }
}
