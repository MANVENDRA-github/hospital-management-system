package com.hospital.auth.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsTest {

    @Test
    void email_already_exists_message_includes_email() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("a@b.c");
        assertThat(ex.getMessage()).contains("a@b.c");
    }

    @Test
    void invalid_credentials_default_message() {
        InvalidCredentialsException ex = new InvalidCredentialsException();
        assertThat(ex.getMessage()).contains("Invalid");
    }
}
