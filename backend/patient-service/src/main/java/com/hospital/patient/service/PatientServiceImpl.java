package com.hospital.patient.service;

import com.hospital.patient.dto.PatientRequest;
import com.hospital.patient.dto.PatientResponse;
import com.hospital.patient.entity.Patient;
import com.hospital.patient.exception.PatientNotFoundException;
import com.hospital.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repository;

    public PatientServiceImpl(PatientRepository repository) {
        this.repository = repository;
    }

    @Override
    public PatientResponse create(PatientRequest request) {
        Patient saved = repository.save(toEntity(request));
        return toResponse(saved);
    }

    @Override
    public PatientResponse update(Long id, PatientRequest request) {
        Patient existing = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        existing.setUserId(request.getUserId());
        existing.setName(request.getName());
        existing.setDateOfBirth(request.getDateOfBirth());
        existing.setGender(request.getGender());
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        return toResponse(repository.save(existing));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Override
    public PatientResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    @Override
    public List<PatientResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private Patient toEntity(PatientRequest r) {
        return Patient.builder()
                .userId(r.getUserId())
                .name(r.getName())
                .dateOfBirth(r.getDateOfBirth())
                .gender(r.getGender())
                .phone(r.getPhone())
                .address(r.getAddress())
                .build();
    }

    private PatientResponse toResponse(Patient p) {
        return PatientResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .name(p.getName())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .phone(p.getPhone())
                .address(p.getAddress())
                .build();
    }
}
