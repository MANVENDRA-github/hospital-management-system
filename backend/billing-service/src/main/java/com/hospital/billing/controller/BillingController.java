package com.hospital.billing.controller;

import com.hospital.billing.dto.BillRequest;
import com.hospital.billing.dto.BillResponse;
import com.hospital.billing.service.BillingService;
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
@RequestMapping("/api/billing")
@Tag(name = "Billing", description = "Bills and payment lifecycle")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a bill manually")
    public ResponseEntity<BillResponse> create(@Valid @RequestBody BillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @Operation(summary = "Fetch a bill by id")
    public ResponseEntity<BillResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getById(id));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark a bill paid")
    public ResponseEntity<BillResponse> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.markPaid(id));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @Operation(summary = "List bills for a patient")
    public ResponseEntity<List<BillResponse>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(billingService.getByPatient(patientId));
    }
}
