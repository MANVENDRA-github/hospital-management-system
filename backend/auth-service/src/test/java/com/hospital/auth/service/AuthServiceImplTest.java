package com.hospital.auth.service;

import com.hospital.auth.dto.AuthResponse;
import com.hospital.auth.dto.LoginRequest;
import com.hospital.auth.dto.RegisterRequest;
import com.hospital.auth.entity.Role;
import com.hospital.auth.entity.User;
import com.hospital.auth.exception.EmailAlreadyExistsException;
import com.hospital.auth.exception.InvalidCredentialsException;
import com.hospital.auth.repository.UserRepository;
import com.hospital.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock UserRepository repository;
    @Mock PasswordEncoder encoder;
    @Mock AuthenticationManager authManager;
    @Mock JwtUtil jwtUtil;

    @InjectMocks AuthServiceImpl service;

    private RegisterRequest registerReq;
    private LoginRequest loginReq;

    @BeforeEach
    void setup() {
        registerReq = RegisterRequest.builder().email("u@x.com").password("pw1234").role(Role.PATIENT).build();
        loginReq = LoginRequest.builder().email("u@x.com").password("pw1234").build();
    }

    @Test
    void register_persists_user_with_encoded_password_and_returns_token() {
        when(repository.existsByEmail("u@x.com")).thenReturn(false);
        when(encoder.encode("pw1234")).thenReturn("hash");
        when(repository.save(any(User.class)))
                .thenAnswer(inv -> {
                    User u = inv.getArgument(0);
                    u.setId(42L);
                    return u;
                });
        when(jwtUtil.generate(any(User.class))).thenReturn("the-token");

        AuthResponse response = service.register(registerReq);

        assertThat(response.getToken()).isEqualTo("the-token");
        assertThat(response.getEmail()).isEqualTo("u@x.com");
        assertThat(response.getRole()).isEqualTo(Role.PATIENT);
        verify(repository).save(any(User.class));
    }

    @Test
    void register_rejects_duplicate_email() {
        when(repository.existsByEmail("u@x.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(registerReq))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void login_returns_token_on_valid_credentials() {
        User stored = User.builder().id(1L).email("u@x.com").password("hash").role(Role.PATIENT).build();
        when(repository.findByEmail("u@x.com")).thenReturn(Optional.of(stored));
        when(jwtUtil.generate(stored)).thenReturn("login-token");

        AuthResponse response = service.login(loginReq);

        assertThat(response.getToken()).isEqualTo("login-token");
        assertThat(response.getRole()).isEqualTo(Role.PATIENT);
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_throws_when_authentication_fails() {
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> service.login(loginReq))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throws_when_user_missing_after_auth() {
        when(repository.findByEmail("u@x.com")).thenReturn(Optional.empty());

        AuthenticationManager manager = mock(AuthenticationManager.class);
        AuthServiceImpl svc = new AuthServiceImpl(repository, encoder, manager, jwtUtil);

        assertThatThrownBy(() -> svc.login(loginReq))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
