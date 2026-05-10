package com.hospital.auth.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void email_exists_to_409() {
        ResponseEntity<Map<String, Object>> r = handler.handleEmailExists(new EmailAlreadyExistsException("a@b"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(r.getBody()).containsEntry("status", 409).containsKey("timestamp");
    }

    @Test
    void invalid_credentials_to_401() {
        ResponseEntity<Map<String, Object>> r = handler.handleInvalidCredentials(new InvalidCredentialsException());
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void validation_error_includes_field_messages() throws Exception {
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Object(), "obj");
        br.addError(new FieldError("obj", "email", "must not be blank"));
        MethodParameter mp = new MethodParameter(GlobalExceptionHandlerTest.class.getDeclaredMethod("validation_error_includes_field_messages"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, br);

        ResponseEntity<Map<String, Object>> r = handler.handleValidation(ex);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) r.getBody().get("errors");
        assertThat(errors).containsEntry("email", "must not be blank");
    }

    @Test
    void generic_exception_to_500() {
        ResponseEntity<Map<String, Object>> r = handler.handleGeneric(new RuntimeException("boom"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(r.getBody()).containsEntry("message", "boom");
    }
}
