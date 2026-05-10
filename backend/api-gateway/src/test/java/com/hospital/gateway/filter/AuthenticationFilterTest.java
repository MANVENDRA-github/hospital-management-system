package com.hospital.gateway.filter;

import com.hospital.gateway.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationFilterTest {

    private static final String SECRET_B64 = Base64.getEncoder()
            .encodeToString("test-secret-test-secret-test-secret-test-secret-1234567890".getBytes());

    private final JwtUtil jwtUtil = new JwtUtil(SECRET_B64);
    private final AuthenticationFilter authFilter = new AuthenticationFilter(jwtUtil);
    private final GatewayFilter filter = authFilter.apply(new AuthenticationFilter.Config());

    private String validToken() {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_B64));
        return Jwts.builder().subject("u@x.com").claims(Map.of("role", "ADMIN"))
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 60000)).signWith(key).compact();
    }

    @Test
    void rejects_request_without_authorization_header() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/patients").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rejects_malformed_authorization_header() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/patients")
                .header(HttpHeaders.AUTHORIZATION, "Token abc")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rejects_invalid_token() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/patients")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-jwt")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void forwards_valid_request_with_user_headers() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/patients")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken())
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(org.mockito.ArgumentMatchers.any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(org.mockito.ArgumentMatchers.any(ServerWebExchange.class));
    }
}
