package com.hospital.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hospital.appointment.dto.AppointmentRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.entity.AppointmentStatus;
import com.hospital.appointment.exception.AppointmentNotFoundException;
import com.hospital.appointment.exception.GlobalExceptionHandler;
import com.hospital.appointment.exception.InvalidAppointmentException;
import com.hospital.appointment.service.AppointmentService;
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

class AppointmentControllerTest {

    private final AppointmentService service = mock(AppointmentService.class);
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(json);
        mvc = MockMvcBuilders.standaloneSetup(new AppointmentController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();
    }

    private AppointmentResponse sample(long id, AppointmentStatus status) {
        return AppointmentResponse.builder().id(id).patientId(1L).doctorId(2L)
                .appointmentDate(LocalDateTime.now().plusDays(1)).status(status).build();
    }

    @Test
    void book_returns_201() throws Exception {
        AppointmentRequest req = AppointmentRequest.builder().patientId(1L).doctorId(2L).appointmentDate(LocalDateTime.now().plusDays(1)).build();
        when(service.book(any())).thenReturn(sample(99L, AppointmentStatus.SCHEDULED));

        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void book_returns_400_when_invalid_body() throws Exception {
        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void book_returns_400_when_invalid_appointment() throws Exception {
        AppointmentRequest req = AppointmentRequest.builder().patientId(1L).doctorId(2L).appointmentDate(LocalDateTime.now().plusDays(1)).build();
        when(service.book(any())).thenThrow(new InvalidAppointmentException("past"));

        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancel_returns_200() throws Exception {
        when(service.cancel(5L)).thenReturn(sample(5L, AppointmentStatus.CANCELLED));
        mvc.perform(put("/api/appointments/5/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void complete_returns_200() throws Exception {
        when(service.complete(5L)).thenReturn(sample(5L, AppointmentStatus.COMPLETED));
        mvc.perform(put("/api/appointments/5/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getById_returns_200_or_404() throws Exception {
        when(service.getById(eq(7L))).thenReturn(sample(7L, AppointmentStatus.SCHEDULED));
        mvc.perform(get("/api/appointments/7")).andExpect(status().isOk());

        doThrow(new AppointmentNotFoundException(8L)).when(service).getById(8L);
        mvc.perform(get("/api/appointments/8")).andExpect(status().isNotFound());
    }

    @Test
    void getAll_and_filters() throws Exception {
        when(service.getAll()).thenReturn(List.of(sample(1L, AppointmentStatus.SCHEDULED)));
        when(service.getByPatient(10L)).thenReturn(List.of(sample(2L, AppointmentStatus.SCHEDULED)));
        when(service.getByDoctor(20L)).thenReturn(List.of(sample(3L, AppointmentStatus.SCHEDULED)));

        mvc.perform(get("/api/appointments")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/appointments/patient/10")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/appointments/doctor/20")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }
}
