package com.hospital.lab.service;

import com.hospital.lab.dto.LabResultRequest;
import com.hospital.lab.dto.LabTestRequest;
import com.hospital.lab.dto.LabTestResponse;
import com.hospital.lab.entity.LabTest;
import com.hospital.lab.entity.LabTestStatus;
import com.hospital.lab.exception.LabTestNotFoundException;
import com.hospital.lab.repository.LabTestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LaboratoryServiceImpl implements LaboratoryService {

    private final LabTestRepository repository;

    public LaboratoryServiceImpl(LabTestRepository repository) {
        this.repository = repository;
    }

    @Override
    public LabTestResponse bookTest(LabTestRequest request) {
        LabTest saved = repository.save(LabTest.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .testName(request.getTestName())
                .testDate(request.getTestDate())
                .status(LabTestStatus.PENDING)
                .build());
        return toResponse(saved);
    }

    @Override
    public LabTestResponse uploadResult(Long id, LabResultRequest request) {
        LabTest test = repository.findById(id)
                .orElseThrow(() -> new LabTestNotFoundException(id));
        test.setResult(request.getResult());
        test.setStatus(LabTestStatus.COMPLETED);
        return toResponse(repository.save(test));
    }

    @Override
    public LabTestResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new LabTestNotFoundException(id));
    }

    @Override
    public List<LabTestResponse> getByPatient(Long patientId) {
        return repository.findByPatientId(patientId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<LabTestResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private LabTestResponse toResponse(LabTest t) {
        return LabTestResponse.builder()
                .id(t.getId())
                .patientId(t.getPatientId())
                .doctorId(t.getDoctorId())
                .testName(t.getTestName())
                .testDate(t.getTestDate())
                .result(t.getResult())
                .status(t.getStatus())
                .build();
    }
}
