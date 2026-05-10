package com.hospital.billing.event;

import com.hospital.billing.service.BillingService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AppointmentCompletedListenerTest {

    @Test
    void onAppointmentCompleted_delegates_to_service() {
        BillingService service = mock(BillingService.class);
        AppointmentCompletedListener listener = new AppointmentCompletedListener(service);
        AppointmentCompletedEvent event = AppointmentCompletedEvent.builder()
                .appointmentId(1L).patientId(2L).doctorId(3L).completedAt(Instant.now()).build();

        listener.onAppointmentCompleted(event);

        verify(service).generateBillFromAppointment(event);
    }
}
