package com.hospital.patient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {

    private Long userId;

    @NotBlank
    private String name;

    private LocalDate dateOfBirth;

    private String gender;

    private String phone;

    private String address;
}
