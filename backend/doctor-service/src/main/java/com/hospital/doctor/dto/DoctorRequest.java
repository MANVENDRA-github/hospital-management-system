package com.hospital.doctor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRequest {

    private Long userId;

    @NotBlank
    private String name;

    @NotBlank
    private String specialization;

    private String phone;

    @Email
    private String email;
}
