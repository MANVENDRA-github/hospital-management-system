package com.hospital.doctor.service;

import com.hospital.doctor.dto.DoctorRequest;
import com.hospital.doctor.dto.DoctorResponse;

import java.util.List;

public interface DoctorService {
    DoctorResponse create(DoctorRequest request);
    DoctorResponse update(Long id, DoctorRequest request);
    void delete(Long id);
    DoctorResponse getById(Long id);
    List<DoctorResponse> getAll();
    List<DoctorResponse> findBySpecialization(String specialization);
}
