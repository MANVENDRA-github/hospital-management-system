package com.hospital.patient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.patient.dto.PatientRequest;
import com.hospital.patient.dto.PatientResponse;
import com.hospital.patient.exception.GlobalExceptionHandler;
import com.hospital.patient.exception.PatientNotFoundException;
import com.hospital.patient.service.PatientService;
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

class PatientControllerTest {

    private final PatientService service = mock(PatientService.class);
    private final ObjectMapper json = new ObjectMapper();
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new PatientController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void create_returns_201() throws Exception {
        PatientRequest req = PatientRequest.builder().name("Alice").build();
        when(service.create(any())).thenReturn(PatientResponse.builder().id(1L).name("Alice").build());

        mvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_returns_400_when_name_blank() throws Exception {
        PatientRequest bad = PatientRequest.builder().name("").build();

        mvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returns_200() throws Exception {
        PatientRequest req = PatientRequest.builder().name("Bob").build();
        when(service.update(eq(5L), any())).thenReturn(PatientResponse.builder().id(5L).name("Bob").build());

        mvc.perform(put("/api/patients/5").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob"));
    }

    @Test
    void delete_returns_204() throws Exception {
        mvc.perform(delete("/api/patients/5"))
                .andExpect(status().isNoContent());
        verify(service).delete(5L);
    }

    @Test
    void getById_returns_200() throws Exception {
        when(service.getById(7L)).thenReturn(PatientResponse.builder().id(7L).name("X").build());

        mvc.perform(get("/api/patients/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void getById_returns_404_when_missing() throws Exception {
        doThrow(new PatientNotFoundException(8L)).when(service).getById(8L);

        mvc.perform(get("/api/patients/8")).andExpect(status().isNotFound());
    }

    @Test
    void getAll_returns_list() throws Exception {
        when(service.getAll()).thenReturn(List.of(PatientResponse.builder().id(1L).name("A").build()));

        mvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
