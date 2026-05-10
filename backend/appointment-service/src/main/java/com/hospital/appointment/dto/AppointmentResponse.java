package com.hospital.appointment.dto;

import com.hospital.appointment.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private LocalDateTime appointmentDate;
    private String reason;
    private AppointmentStatus status;
}
