package com.hospital.lab.exception;

public class LabTestNotFoundException extends RuntimeException {
    public LabTestNotFoundException(Long id) {
        super("Lab test not found: " + id);
    }
}
