package com.hospital.doctor.service;

import com.hospital.doctor.dto.DoctorRequest;
import com.hospital.doctor.dto.DoctorResponse;
import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.exception.DoctorNotFoundException;
import com.hospital.doctor.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock DoctorRepository repository;
    @InjectMocks DoctorServiceImpl service;

    private DoctorRequest req;
    private Doctor stored;

    @BeforeEach
    void setup() {
        req = DoctorRequest.builder().userId(1L).name("Dr. House").specialization("Diagnostics").phone("p").email("h@x.com").build();
        stored = Doctor.builder().id(7L).userId(1L).name("Dr. House").specialization("Diagnostics").phone("p").email("h@x.com").build();
    }

    @Test
    void create_persists_and_returns_response() {
        when(repository.save(any(Doctor.class))).thenReturn(stored);
        DoctorResponse r = service.create(req);
        assertThat(r.getId()).isEqualTo(7L);
        assertThat(r.getSpecialization()).isEqualTo("Diagnostics");
    }

    @Test
    void update_overwrites_fields() {
        when(repository.findById(7L)).thenReturn(Optional.of(stored));
        when(repository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        DoctorResponse r = service.update(7L, DoctorRequest.builder().name("Dr. Wilson").specialization("Oncology").build());

        assertThat(r.getName()).isEqualTo("Dr. Wilson");
        assertThat(r.getSpecialization()).isEqualTo("Oncology");
    }

    @Test
    void update_throws_when_missing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, req)).isInstanceOf(DoctorNotFoundException.class);
    }

    @Test
    void delete_removes_when_present_throws_when_missing() {
        when(repository.existsById(7L)).thenReturn(true);
        service.delete(7L);
        verify(repository).deleteById(7L);

        when(repository.existsById(8L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(8L)).isInstanceOf(DoctorNotFoundException.class);
        verify(repository, never()).deleteById(8L);
    }

    @Test
    void getById_returns_or_throws() {
        when(repository.findById(7L)).thenReturn(Optional.of(stored));
        assertThat(service.getById(7L).getName()).isEqualTo("Dr. House");

        when(repository.findById(8L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(8L)).isInstanceOf(DoctorNotFoundException.class);
    }

    @Test
    void getAll_maps_each() {
        when(repository.findAll()).thenReturn(List.of(stored));
        assertThat(service.getAll()).hasSize(1);
    }

    @Test
    void findBySpecialization_delegates_case_insensitive() {
        when(repository.findBySpecializationIgnoreCase("Diagnostics")).thenReturn(List.of(stored));
        assertThat(service.findBySpecialization("Diagnostics")).hasSize(1);
    }
}
