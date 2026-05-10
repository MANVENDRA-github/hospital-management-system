package com.hospital.appointment.service;

import com.hospital.appointment.client.DoctorClient;
import com.hospital.appointment.client.DoctorSummary;
import com.hospital.appointment.client.PatientClient;
import com.hospital.appointment.client.PatientSummary;
import com.hospital.appointment.dto.AppointmentRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.AppointmentStatus;
import com.hospital.appointment.event.AppointmentCompletedEvent;
import com.hospital.appointment.event.AppointmentEventPublisher;
import com.hospital.appointment.exception.AppointmentNotFoundException;
import com.hospital.appointment.exception.InvalidAppointmentException;
import com.hospital.appointment.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock AppointmentRepository repository;
    @Mock PatientClient patientClient;
    @Mock DoctorClient doctorClient;
    @Mock AppointmentEventPublisher eventPublisher;

    @InjectMocks AppointmentServiceImpl service;

    private AppointmentRequest req;

    @BeforeEach
    void setup() {
        req = AppointmentRequest.builder()
                .patientId(1L).doctorId(2L)
                .appointmentDate(LocalDateTime.now().plusDays(1))
                .reason("checkup").build();
    }

    @Test
    void book_validates_refs_and_persists() {
        when(patientClient.getById(1L)).thenReturn(PatientSummary.builder().id(1L).name("p").build());
        when(doctorClient.getById(2L)).thenReturn(DoctorSummary.builder().id(2L).name("d").specialization("s").build());
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });

        AppointmentResponse response = service.book(req);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(patientClient).getById(1L);
        verify(doctorClient).getById(2L);
    }

    @Test
    void book_rejects_past_dates() {
        AppointmentRequest past = AppointmentRequest.builder()
                .patientId(1L).doctorId(2L)
                .appointmentDate(LocalDateTime.now().minusDays(1))
                .reason("late").build();

        assertThatThrownBy(() -> service.book(past)).isInstanceOf(InvalidAppointmentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void cancel_flips_status_when_not_completed() {
        Appointment existing = Appointment.builder().id(1L).status(AppointmentStatus.SCHEDULED).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse r = service.cancel(1L);

        assertThat(r.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancel_rejects_completed() {
        Appointment existing = Appointment.builder().id(1L).status(AppointmentStatus.COMPLETED).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        assertThatThrownBy(() -> service.cancel(1L)).isInstanceOf(InvalidAppointmentException.class);
    }

    @Test
    void cancel_throws_when_missing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cancel(99L)).isInstanceOf(AppointmentNotFoundException.class);
    }

    @Test
    void complete_publishes_event_when_not_cancelled() {
        Appointment existing = Appointment.builder().id(1L).patientId(2L).doctorId(3L).status(AppointmentStatus.SCHEDULED).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse r = service.complete(1L);

        assertThat(r.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        verify(eventPublisher).publishCompleted(any(AppointmentCompletedEvent.class));
    }

    @Test
    void complete_rejects_cancelled_appointment() {
        Appointment existing = Appointment.builder().id(1L).status(AppointmentStatus.CANCELLED).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        assertThatThrownBy(() -> service.complete(1L)).isInstanceOf(InvalidAppointmentException.class);
        verify(eventPublisher, never()).publishCompleted(any());
    }

    @Test
    void getById_returns_or_throws() {
        when(repository.findById(1L)).thenReturn(Optional.of(Appointment.builder().id(1L).status(AppointmentStatus.SCHEDULED).build()));
        assertThat(service.getById(1L).getId()).isEqualTo(1L);

        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(2L)).isInstanceOf(AppointmentNotFoundException.class);
    }

    @Test
    void list_helpers_delegate() {
        Appointment a = Appointment.builder().id(1L).patientId(10L).doctorId(20L).status(AppointmentStatus.SCHEDULED).build();
        when(repository.findAll()).thenReturn(List.of(a));
        when(repository.findByPatientId(10L)).thenReturn(List.of(a));
        when(repository.findByDoctorId(20L)).thenReturn(List.of(a));

        assertThat(service.getAll()).hasSize(1);
        assertThat(service.getByPatient(10L)).hasSize(1);
        assertThat(service.getByDoctor(20L)).hasSize(1);
    }
}
