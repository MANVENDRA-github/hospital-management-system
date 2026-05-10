package com.hospital.doctor.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void not_found_to_404() {
        ResponseEntity<Map<String, Object>> r = handler.handleNotFound(new DoctorNotFoundException(1L));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void access_denied_to_403() {
        ResponseEntity<Map<String, Object>> r = handler.handleAccessDenied(new AccessDeniedException("nope"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void validation_to_400_with_errors() throws Exception {
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Object(), "obj");
        br.addError(new FieldError("obj", "name", "must not be blank"));
        MethodParameter mp = new MethodParameter(GlobalExceptionHandlerTest.class.getDeclaredMethod("validation_to_400_with_errors"), -1);
        ResponseEntity<Map<String, Object>> r = handler.handleValidation(new MethodArgumentNotValidException(mp, br));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void generic_to_500() {
        assertThat(handler.handleGeneric(new RuntimeException("x")).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
