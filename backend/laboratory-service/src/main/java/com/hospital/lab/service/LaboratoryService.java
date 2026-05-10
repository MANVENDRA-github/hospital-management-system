package com.hospital.lab.service;

import com.hospital.lab.dto.LabResultRequest;
import com.hospital.lab.dto.LabTestRequest;
import com.hospital.lab.dto.LabTestResponse;

import java.util.List;

public interface LaboratoryService {
    LabTestResponse bookTest(LabTestRequest request);
    LabTestResponse uploadResult(Long id, LabResultRequest request);
    LabTestResponse getById(Long id);
    List<LabTestResponse> getByPatient(Long patientId);
    List<LabTestResponse> getAll();
}
