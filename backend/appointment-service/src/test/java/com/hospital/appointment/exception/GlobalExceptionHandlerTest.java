package com.hospital.appointment.exception;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void not_found_to_404() {
        ResponseEntity<Map<String, Object>> r = handler.handleNotFound(new AppointmentNotFoundException(1L));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void invalid_to_400() {
        ResponseEntity<Map<String, Object>> r = handler.handleInvalid(new InvalidAppointmentException("bad"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void feign_not_found_to_400() {
        Request request = Request.create(Request.HttpMethod.GET, "/x", new HashMap<>(), null, StandardCharsets.UTF_8, null);
        Response feignResponse = Response.builder().status(404).request(request).build();
        FeignException.NotFound ex = (FeignException.NotFound) FeignException.errorStatus("getById", feignResponse);
        ResponseEntity<Map<String, Object>> r = handler.handleFeignNotFound(ex);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void access_denied_to_403() {
        ResponseEntity<Map<String, Object>> r = handler.handleAccessDenied(new AccessDeniedException("nope"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void validation_to_400() throws Exception {
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Object(), "obj");
        br.addError(new FieldError("obj", "patientId", "must not be null"));
        MethodParameter mp = new MethodParameter(GlobalExceptionHandlerTest.class.getDeclaredMethod("validation_to_400"), -1);
        ResponseEntity<Map<String, Object>> r = handler.handleValidation(new MethodArgumentNotValidException(mp, br));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void generic_to_500() {
        assertThat(handler.handleGeneric(new RuntimeException("boom")).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
