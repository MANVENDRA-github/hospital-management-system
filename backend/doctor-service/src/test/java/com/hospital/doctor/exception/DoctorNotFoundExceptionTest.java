package com.hospital.doctor.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorNotFoundExceptionTest {
    @Test
    void message_contains_id() {
        assertThat(new DoctorNotFoundException(99L).getMessage()).contains("99");
    }
}
