package com.hospital.billing.service;

import com.hospital.billing.dto.BillRequest;
import com.hospital.billing.dto.BillResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.PaymentStatus;
import com.hospital.billing.event.AppointmentCompletedEvent;
import com.hospital.billing.exception.BillNotFoundException;
import com.hospital.billing.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BillingServiceImplTest {

    private final BillRepository repository = mock(BillRepository.class);
    private final BillingServiceImpl service = new BillingServiceImpl(repository, new BigDecimal("500.00"));

    private Bill stored;

    @BeforeEach
    void setup() {
        stored = Bill.builder().id(1L).patientId(2L).appointmentId(3L)
                .amount(new BigDecimal("100.00")).description("d")
                .paymentStatus(PaymentStatus.PENDING).createdAt(LocalDateTime.now()).build();
    }

    @Test
    void create_persists_with_pending_status() {
        when(repository.save(any(Bill.class))).thenAnswer(inv -> {
            Bill b = inv.getArgument(0);
            b.setId(7L);
            return b;
        });

        BillResponse r = service.create(BillRequest.builder()
                .patientId(2L).appointmentId(3L).amount(new BigDecimal("100.00")).description("d").build());

        assertThat(r.getId()).isEqualTo(7L);
        assertThat(r.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void getById_or_throws() {
        when(repository.findById(1L)).thenReturn(Optional.of(stored));
        assertThat(service.getById(1L).getId()).isEqualTo(1L);

        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(2L)).isInstanceOf(BillNotFoundException.class);
    }

    @Test
    void markPaid_flips_status_or_throws() {
        when(repository.findById(1L)).thenReturn(Optional.of(stored));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BillResponse r = service.markPaid(1L);
        assertThat(r.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);

        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.markPaid(2L)).isInstanceOf(BillNotFoundException.class);
    }

    @Test
    void getByPatient_delegates() {
        when(repository.findByPatientId(2L)).thenReturn(List.of(stored));
        assertThat(service.getByPatient(2L)).hasSize(1);
    }

    @Test
    void generateBillFromAppointment_creates_when_not_exists() {
        when(repository.existsByAppointmentId(99L)).thenReturn(false);
        when(repository.save(any(Bill.class))).thenAnswer(inv -> {
            Bill b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        AppointmentCompletedEvent event = AppointmentCompletedEvent.builder()
                .appointmentId(99L).patientId(2L).doctorId(3L).completedAt(Instant.now()).build();

        service.generateBillFromAppointment(event);
        verify(repository).save(any(Bill.class));
    }

    @Test
    void generateBillFromAppointment_skips_when_exists() {
        when(repository.existsByAppointmentId(99L)).thenReturn(true);

        AppointmentCompletedEvent event = AppointmentCompletedEvent.builder()
                .appointmentId(99L).patientId(2L).doctorId(3L).completedAt(Instant.now()).build();

        service.generateBillFromAppointment(event);
        verify(repository, never()).save(any());
    }
}
