package com.hospital.lab.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LabTestNotFoundExceptionTest {
    @Test
    void message_contains_id() {
        assertThat(new LabTestNotFoundException(7L).getMessage()).contains("7");
    }
}
