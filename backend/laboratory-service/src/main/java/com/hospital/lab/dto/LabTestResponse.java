package com.hospital.lab.dto;

import com.hospital.lab.entity.LabTestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestResponse {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private String testName;
    private LocalDateTime testDate;
    private String result;
    private LabTestStatus status;
}
