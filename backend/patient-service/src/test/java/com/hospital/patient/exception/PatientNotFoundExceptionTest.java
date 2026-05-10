package com.hospital.patient.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PatientNotFoundExceptionTest {
    @Test
    void message_contains_id() {
        PatientNotFoundException ex = new PatientNotFoundException(42L);
        assertThat(ex.getMessage()).contains("42");
    }
}
