package com.hospital.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-service", path = "/api/doctors")
public interface DoctorClient {

    @GetMapping("/{id}")
    DoctorSummary getById(@PathVariable("id") Long id);
}
