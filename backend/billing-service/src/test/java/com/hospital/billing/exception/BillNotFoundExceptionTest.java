package com.hospital.billing.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BillNotFoundExceptionTest {
    @Test
    void message_contains_id() {
        assertThat(new BillNotFoundException(42L).getMessage()).contains("42");
    }
}
