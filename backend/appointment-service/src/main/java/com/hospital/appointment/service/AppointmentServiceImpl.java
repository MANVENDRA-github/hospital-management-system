package com.hospital.appointment.service;

import com.hospital.appointment.client.DoctorClient;
import com.hospital.appointment.client.PatientClient;
import com.hospital.appointment.dto.AppointmentRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.AppointmentStatus;
import com.hospital.appointment.event.AppointmentCompletedEvent;
import com.hospital.appointment.event.AppointmentEventPublisher;
import com.hospital.appointment.exception.AppointmentNotFoundException;
import com.hospital.appointment.exception.InvalidAppointmentException;
import com.hospital.appointment.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository repository;
    private final PatientClient patientClient;
    private final DoctorClient doctorClient;
    private final AppointmentEventPublisher eventPublisher;

    public AppointmentServiceImpl(AppointmentRepository repository,
                                  PatientClient patientClient,
                                  DoctorClient doctorClient,
                                  AppointmentEventPublisher eventPublisher) {
        this.repository = repository;
        this.patientClient = patientClient;
        this.doctorClient = doctorClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AppointmentResponse book(AppointmentRequest request) {
        if (request.getAppointmentDate().isBefore(LocalDateTime.now())) {
            throw new InvalidAppointmentException("Appointment date must be in the future");
        }
        // Validate that referenced patient and doctor exist (Feign call into the other services).
        patientClient.getById(request.getPatientId());
        doctorClient.getById(request.getDoctorId());

        Appointment saved = repository.save(Appointment.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .appointmentDate(request.getAppointmentDate())
                .reason(request.getReason())
                .status(AppointmentStatus.SCHEDULED)
                .build());
        log.info("Booked appointment {} for patient {} with doctor {}", saved.getId(),
                saved.getPatientId(), saved.getDoctorId());
        return toResponse(saved);
    }

    @Override
    public AppointmentResponse cancel(Long id) {
        Appointment appointment = findOrThrow(id);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentException("Completed appointments cannot be cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(repository.save(appointment));
    }

    @Override
    public AppointmentResponse complete(Long id) {
        Appointment appointment = findOrThrow(id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentException("Cancelled appointments cannot be completed");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = repository.save(appointment);
        eventPublisher.publishCompleted(AppointmentCompletedEvent.builder()
                .appointmentId(saved.getId())
                .patientId(saved.getPatientId())
                .doctorId(saved.getDoctorId())
                .completedAt(Instant.now())
                .build());
        return toResponse(saved);
    }

    @Override
    public AppointmentResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<AppointmentResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<AppointmentResponse> getByPatient(Long patientId) {
        return repository.findByPatientId(patientId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<AppointmentResponse> getByDoctor(Long doctorId) {
        return repository.findByDoctorId(doctorId).stream().map(this::toResponse).toList();
    }

    private Appointment findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    private AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatientId())
                .doctorId(a.getDoctorId())
                .appointmentDate(a.getAppointmentDate())
                .reason(a.getReason())
                .status(a.getStatus())
                .build();
    }
}
