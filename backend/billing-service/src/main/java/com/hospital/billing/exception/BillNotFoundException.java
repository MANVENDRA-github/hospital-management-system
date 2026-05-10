package com.hospital.billing.exception;

public class BillNotFoundException extends RuntimeException {
    public BillNotFoundException(Long id) {
        super("Bill not found: " + id);
    }
}
