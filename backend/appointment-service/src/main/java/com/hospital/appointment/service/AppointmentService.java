package com.hospital.appointment.service;

import com.hospital.appointment.dto.AppointmentRequest;
import com.hospital.appointment.dto.AppointmentResponse;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse book(AppointmentRequest request);
    AppointmentResponse cancel(Long id);
    AppointmentResponse complete(Long id);
    AppointmentResponse getById(Long id);
    List<AppointmentResponse> getAll();
    List<AppointmentResponse> getByPatient(Long patientId);
    List<AppointmentResponse> getByDoctor(Long doctorId);
}
