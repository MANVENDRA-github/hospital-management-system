package com.hospital.appointment.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsTest {

    @Test
    void appointment_not_found_message_contains_id() {
        assertThat(new AppointmentNotFoundException(7L).getMessage()).contains("7");
    }

    @Test
    void invalid_appointment_passes_message() {
        assertThat(new InvalidAppointmentException("nope").getMessage()).isEqualTo("nope");
    }
}
