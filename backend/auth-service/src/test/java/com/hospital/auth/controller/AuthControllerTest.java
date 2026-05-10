package com.hospital.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.auth.dto.AuthResponse;
import com.hospital.auth.dto.LoginRequest;
import com.hospital.auth.dto.RegisterRequest;
import com.hospital.auth.entity.Role;
import com.hospital.auth.exception.EmailAlreadyExistsException;
import com.hospital.auth.exception.GlobalExceptionHandler;
import com.hospital.auth.exception.InvalidCredentialsException;
import com.hospital.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private final AuthService service = mock(AuthService.class);
    private final ObjectMapper json = new ObjectMapper();
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_returns_201_with_token() throws Exception {
        RegisterRequest req = RegisterRequest.builder().email("u@x.com").password("pw1234").role(Role.PATIENT).build();
        when(service.register(any())).thenReturn(AuthResponse.builder().token("t").email("u@x.com").role(Role.PATIENT).build());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("t"))
                .andExpect(jsonPath("$.role").value("PATIENT"));
    }

    @Test
    void register_returns_409_on_duplicate_email() throws Exception {
        RegisterRequest req = RegisterRequest.builder().email("u@x.com").password("pw1234").role(Role.PATIENT).build();
        when(service.register(any())).thenThrow(new EmailAlreadyExistsException("u@x.com"));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_returns_400_on_invalid_body() throws Exception {
        RegisterRequest bad = RegisterRequest.builder().email("not-email").password("123").role(null).build();

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns_200_with_token() throws Exception {
        LoginRequest req = LoginRequest.builder().email("u@x.com").password("pw1234").build();
        when(service.login(any())).thenReturn(AuthResponse.builder().token("t").email("u@x.com").role(Role.ADMIN).build());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("t"));
    }

    @Test
    void login_returns_401_on_invalid_credentials() throws Exception {
        LoginRequest req = LoginRequest.builder().email("u@x.com").password("pw1234").build();
        when(service.login(any())).thenThrow(new InvalidCredentialsException());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
