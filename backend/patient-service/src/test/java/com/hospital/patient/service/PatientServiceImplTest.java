package com.hospital.patient.service;

import com.hospital.patient.dto.PatientRequest;
import com.hospital.patient.dto.PatientResponse;
import com.hospital.patient.entity.Patient;
import com.hospital.patient.exception.PatientNotFoundException;
import com.hospital.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock PatientRepository repository;
    @InjectMocks PatientServiceImpl service;

    private PatientRequest req;
    private Patient stored;

    @BeforeEach
    void setup() {
        req = PatientRequest.builder()
                .userId(1L).name("Alice").dateOfBirth(LocalDate.of(1990, 1, 2))
                .gender("F").phone("123").address("Earth").build();
        stored = Patient.builder()
                .id(10L).userId(1L).name("Alice").dateOfBirth(LocalDate.of(1990, 1, 2))
                .gender("F").phone("123").address("Earth").build();
    }

    @Test
    void create_persists_and_maps_to_response() {
        when(repository.save(any(Patient.class))).thenReturn(stored);

        PatientResponse response = service.create(req);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Alice");
    }

    @Test
    void update_overwrites_fields() {
        when(repository.findById(10L)).thenReturn(Optional.of(stored));
        when(repository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        PatientResponse response = service.update(10L,
                PatientRequest.builder().name("Bob").userId(2L).gender("M").build());

        assertThat(response.getName()).isEqualTo("Bob");
        assertThat(response.getUserId()).isEqualTo(2L);
        assertThat(response.getGender()).isEqualTo("M");
    }

    @Test
    void update_throws_when_missing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, req)).isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void delete_removes_when_present() {
        when(repository.existsById(10L)).thenReturn(true);
        service.delete(10L);
        verify(repository).deleteById(10L);
    }

    @Test
    void delete_throws_when_missing() {
        when(repository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(PatientNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void getById_returns_patient_or_throws() {
        when(repository.findById(10L)).thenReturn(Optional.of(stored));
        assertThat(service.getById(10L).getName()).isEqualTo("Alice");

        when(repository.findById(11L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(11L)).isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void getAll_maps_each_entity() {
        when(repository.findAll()).thenReturn(List.of(stored));
        List<PatientResponse> all = service.getAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getId()).isEqualTo(10L);
    }
}
