package com.hospital.billing.service;

import com.hospital.billing.dto.BillRequest;
import com.hospital.billing.dto.BillResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.PaymentStatus;
import com.hospital.billing.event.AppointmentCompletedEvent;
import com.hospital.billing.exception.BillNotFoundException;
import com.hospital.billing.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BillingServiceImpl implements BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceImpl.class);

    private final BillRepository repository;
    private final BigDecimal defaultAppointmentCharge;

    public BillingServiceImpl(BillRepository repository,
                              @Value("${app.billing.default-appointment-charge:500.00}")
                              BigDecimal defaultAppointmentCharge) {
        this.repository = repository;
        this.defaultAppointmentCharge = defaultAppointmentCharge;
    }

    @Override
    public BillResponse create(BillRequest request) {
        Bill saved = repository.save(Bill.builder()
                .patientId(request.getPatientId())
                .appointmentId(request.getAppointmentId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());
        return toResponse(saved);
    }

    @Override
    public BillResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new BillNotFoundException(id));
    }

    @Override
    public BillResponse markPaid(Long id) {
        Bill bill = repository.findById(id)
                .orElseThrow(() -> new BillNotFoundException(id));
        bill.setPaymentStatus(PaymentStatus.PAID);
        return toResponse(repository.save(bill));
    }

    @Override
    public List<BillResponse> getByPatient(Long patientId) {
        return repository.findByPatientId(patientId).stream().map(this::toResponse).toList();
    }

    @Override
    public void generateBillFromAppointment(AppointmentCompletedEvent event) {
        if (repository.existsByAppointmentId(event.getAppointmentId())) {
            log.info("Bill for appointmentId={} already exists, skipping", event.getAppointmentId());
            return;
        }
        Bill saved = repository.save(Bill.builder()
                .patientId(event.getPatientId())
                .appointmentId(event.getAppointmentId())
                .amount(defaultAppointmentCharge)
                .description("Auto-generated bill for completed appointment #" + event.getAppointmentId())
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());
        log.info("Auto-generated bill {} for appointmentId={}", saved.getId(), event.getAppointmentId());
    }

    private BillResponse toResponse(Bill b) {
        return BillResponse.builder()
                .id(b.getId())
                .patientId(b.getPatientId())
                .appointmentId(b.getAppointmentId())
                .amount(b.getAmount())
                .description(b.getDescription())
                .paymentStatus(b.getPaymentStatus())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
