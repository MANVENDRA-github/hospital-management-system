package com.hospital.patient.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(Long id) {
        super("Patient not found: " + id);
    }
}
