package com.hospital.doctor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.doctor.dto.DoctorRequest;
import com.hospital.doctor.dto.DoctorResponse;
import com.hospital.doctor.exception.DoctorNotFoundException;
import com.hospital.doctor.exception.GlobalExceptionHandler;
import com.hospital.doctor.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DoctorControllerTest {

    private final DoctorService service = mock(DoctorService.class);
    private final ObjectMapper json = new ObjectMapper();
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new DoctorController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void create_returns_201() throws Exception {
        DoctorRequest req = DoctorRequest.builder().name("D").specialization("S").build();
        when(service.create(any())).thenReturn(DoctorResponse.builder().id(1L).name("D").specialization("S").build());
        mvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_returns_400_when_invalid() throws Exception {
        DoctorRequest bad = DoctorRequest.builder().name("").specialization("").build();
        mvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returns_200() throws Exception {
        DoctorRequest req = DoctorRequest.builder().name("D").specialization("S").build();
        when(service.update(eq(5L), any())).thenReturn(DoctorResponse.builder().id(5L).name("D").specialization("S").build());
        mvc.perform(put("/api/doctors/5").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns_204() throws Exception {
        mvc.perform(delete("/api/doctors/5")).andExpect(status().isNoContent());
        verify(service).delete(5L);
    }

    @Test
    void getById_returns_200_or_404() throws Exception {
        when(service.getById(7L)).thenReturn(DoctorResponse.builder().id(7L).name("D").specialization("S").build());
        mvc.perform(get("/api/doctors/7")).andExpect(status().isOk());

        doThrow(new DoctorNotFoundException(8L)).when(service).getById(8L);
        mvc.perform(get("/api/doctors/8")).andExpect(status().isNotFound());
    }

    @Test
    void getAll_returns_list() throws Exception {
        when(service.getAll()).thenReturn(List.of(DoctorResponse.builder().id(1L).name("D").specialization("S").build()));
        mvc.perform(get("/api/doctors")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void findBySpecialization_returns_list() throws Exception {
        when(service.findBySpecialization("Cardiology")).thenReturn(List.of(DoctorResponse.builder().id(1L).name("D").specialization("Cardiology").build()));
        mvc.perform(get("/api/doctors/specialization/Cardiology")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }
}
