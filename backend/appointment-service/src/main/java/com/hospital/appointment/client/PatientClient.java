package com.hospital.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "patient-service", path = "/api/patients")
public interface PatientClient {

    @GetMapping("/{id}")
    PatientSummary getById(@PathVariable("id") Long id);
}
