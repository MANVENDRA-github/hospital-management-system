package com.hospital.doctor.service;

import com.hospital.doctor.dto.DoctorRequest;
import com.hospital.doctor.dto.DoctorResponse;
import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.exception.DoctorNotFoundException;
import com.hospital.doctor.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository repository;

    public DoctorServiceImpl(DoctorRepository repository) {
        this.repository = repository;
    }

    @Override
    public DoctorResponse create(DoctorRequest request) {
        return toResponse(repository.save(toEntity(request)));
    }

    @Override
    public DoctorResponse update(Long id, DoctorRequest request) {
        Doctor existing = repository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
        existing.setUserId(request.getUserId());
        existing.setName(request.getName());
        existing.setSpecialization(request.getSpecialization());
        existing.setPhone(request.getPhone());
        existing.setEmail(request.getEmail());
        return toResponse(repository.save(existing));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new DoctorNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Override
    public DoctorResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }

    @Override
    public List<DoctorResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<DoctorResponse> findBySpecialization(String specialization) {
        return repository.findBySpecializationIgnoreCase(specialization).stream()
                .map(this::toResponse)
                .toList();
    }

    private Doctor toEntity(DoctorRequest r) {
        return Doctor.builder()
                .userId(r.getUserId())
                .name(r.getName())
                .specialization(r.getSpecialization())
                .phone(r.getPhone())
                .email(r.getEmail())
                .build();
    }

    private DoctorResponse toResponse(Doctor d) {
        return DoctorResponse.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .name(d.getName())
                .specialization(d.getSpecialization())
                .phone(d.getPhone())
                .email(d.getEmail())
                .build();
    }
}
