package com.hospital.appointment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCompletedEvent implements Serializable {
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private Instant completedAt;
}
