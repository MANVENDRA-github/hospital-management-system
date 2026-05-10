package com.hospital.lab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hospital.lab.dto.LabResultRequest;
import com.hospital.lab.dto.LabTestRequest;
import com.hospital.lab.dto.LabTestResponse;
import com.hospital.lab.entity.LabTestStatus;
import com.hospital.lab.exception.GlobalExceptionHandler;
import com.hospital.lab.exception.LabTestNotFoundException;
import com.hospital.lab.service.LaboratoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LaboratoryControllerTest {

    private final LaboratoryService service = mock(LaboratoryService.class);
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new LaboratoryController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(json))
                .build();
    }

    private LabTestResponse sample(long id, LabTestStatus s) {
        return LabTestResponse.builder().id(id).patientId(2L).doctorId(3L)
                .testName("CBC").testDate(LocalDateTime.now().plusDays(1)).status(s).build();
    }

    @Test
    void bookTest_returns_201() throws Exception {
        LabTestRequest req = LabTestRequest.builder()
                .patientId(2L).doctorId(3L).testName("CBC").testDate(LocalDateTime.now().plusDays(1)).build();
        when(service.bookTest(any())).thenReturn(sample(1L, LabTestStatus.PENDING));

        mvc.perform(post("/api/lab").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void bookTest_returns_400_when_invalid() throws Exception {
        mvc.perform(post("/api/lab").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadResult_returns_200() throws Exception {
        when(service.uploadResult(eq(1L), any())).thenReturn(sample(1L, LabTestStatus.COMPLETED));
        mvc.perform(put("/api/lab/1/result").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(LabResultRequest.builder().result("normal").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getById_or_404() throws Exception {
        when(service.getById(1L)).thenReturn(sample(1L, LabTestStatus.PENDING));
        mvc.perform(get("/api/lab/1")).andExpect(status().isOk());

        doThrow(new LabTestNotFoundException(2L)).when(service).getById(2L);
        mvc.perform(get("/api/lab/2")).andExpect(status().isNotFound());
    }

    @Test
    void getByPatient_returns_list() throws Exception {
        when(service.getByPatient(2L)).thenReturn(List.of(sample(1L, LabTestStatus.PENDING)));
        mvc.perform(get("/api/lab/patient/2")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAll_returns_list() throws Exception {
        when(service.getAll()).thenReturn(List.of(sample(1L, LabTestStatus.PENDING)));
        mvc.perform(get("/api/lab")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }
}
