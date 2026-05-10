package com.hospital.lab.controller;

import com.hospital.lab.dto.LabResultRequest;
import com.hospital.lab.dto.LabTestRequest;
import com.hospital.lab.dto.LabTestResponse;
import com.hospital.lab.service.LaboratoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lab")
@Tag(name = "Laboratory", description = "Lab tests and results")
public class LaboratoryController {

    private final LaboratoryService labService;

    public LaboratoryController(LaboratoryService labService) {
        this.labService = labService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Book a lab test")
    public ResponseEntity<LabTestResponse> bookTest(@Valid @RequestBody LabTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labService.bookTest(request));
    }

    @PutMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Upload lab test result")
    public ResponseEntity<LabTestResponse> uploadResult(@PathVariable Long id,
                                                        @Valid @RequestBody LabResultRequest request) {
        return ResponseEntity.ok(labService.uploadResult(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    @Operation(summary = "Fetch a lab test by id")
    public ResponseEntity<LabTestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(labService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    @Operation(summary = "List tests for a patient")
    public ResponseEntity<List<LabTestResponse>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(labService.getByPatient(patientId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all lab tests")
    public ResponseEntity<List<LabTestResponse>> getAll() {
        return ResponseEntity.ok(labService.getAll());
    }
}
