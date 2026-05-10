package com.hospital.lab.service;

import com.hospital.lab.dto.LabResultRequest;
import com.hospital.lab.dto.LabTestRequest;
import com.hospital.lab.dto.LabTestResponse;
import com.hospital.lab.entity.LabTest;
import com.hospital.lab.entity.LabTestStatus;
import com.hospital.lab.exception.LabTestNotFoundException;
import com.hospital.lab.repository.LabTestRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaboratoryServiceImplTest {

    @Mock LabTestRepository repository;
    @InjectMocks LaboratoryServiceImpl service;

    private LabTest stored;

    @BeforeEach
    void setup() {
        stored = LabTest.builder().id(1L).patientId(2L).doctorId(3L)
                .testName("CBC").testDate(LocalDateTime.now().plusDays(1))
                .status(LabTestStatus.PENDING).build();
    }

    @Test
    void bookTest_persists_with_pending_status() {
        when(repository.save(any(LabTest.class))).thenAnswer(inv -> {
            LabTest t = inv.getArgument(0);
            t.setId(7L);
            return t;
        });

        LabTestResponse r = service.bookTest(LabTestRequest.builder()
                .patientId(2L).doctorId(3L).testName("CBC").testDate(LocalDateTime.now().plusDays(1)).build());

        assertThat(r.getId()).isEqualTo(7L);
        assertThat(r.getStatus()).isEqualTo(LabTestStatus.PENDING);
    }

    @Test
    void uploadResult_completes_or_throws() {
        when(repository.findById(1L)).thenReturn(Optional.of(stored));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LabTestResponse r = service.uploadResult(1L, LabResultRequest.builder().result("normal").build());
        assertThat(r.getStatus()).isEqualTo(LabTestStatus.COMPLETED);
        assertThat(r.getResult()).isEqualTo("normal");

        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.uploadResult(2L, LabResultRequest.builder().result("x").build()))
                .isInstanceOf(LabTestNotFoundException.class);
    }

    @Test
    void getById_or_throws() {
        when(repository.findById(1L)).thenReturn(Optional.of(stored));
        assertThat(service.getById(1L).getId()).isEqualTo(1L);

        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(2L)).isInstanceOf(LabTestNotFoundException.class);
    }

    @Test
    void getByPatient_and_getAll_delegate() {
        when(repository.findByPatientId(2L)).thenReturn(List.of(stored));
        when(repository.findAll()).thenReturn(List.of(stored));

        assertThat(service.getByPatient(2L)).hasSize(1);
        assertThat(service.getAll()).hasSize(1);
    }
}
