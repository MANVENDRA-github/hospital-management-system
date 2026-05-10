package com.hospital.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    private Long id;
    private Long userId;
    private String name;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String address;
}
