package com.hospital.billing.service;

import com.hospital.billing.dto.BillRequest;
import com.hospital.billing.dto.BillResponse;
import com.hospital.billing.event.AppointmentCompletedEvent;

import java.util.List;

public interface BillingService {
    BillResponse create(BillRequest request);
    BillResponse getById(Long id);
    BillResponse markPaid(Long id);
    List<BillResponse> getByPatient(Long patientId);
    void generateBillFromAppointment(AppointmentCompletedEvent event);
}
