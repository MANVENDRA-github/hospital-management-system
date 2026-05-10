package com.hospital.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hospital.billing.dto.BillRequest;
import com.hospital.billing.dto.BillResponse;
import com.hospital.billing.entity.PaymentStatus;
import com.hospital.billing.exception.BillNotFoundException;
import com.hospital.billing.exception.GlobalExceptionHandler;
import com.hospital.billing.service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BillingControllerTest {

    private final BillingService service = mock(BillingService.class);
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new BillingController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(json))
                .build();
    }

    private BillResponse sample(long id, PaymentStatus s) {
        return BillResponse.builder().id(id).patientId(2L).appointmentId(3L)
                .amount(new BigDecimal("100.00")).description("d").paymentStatus(s).createdAt(LocalDateTime.now()).build();
    }

    @Test
    void create_returns_201() throws Exception {
        BillRequest req = BillRequest.builder().patientId(2L).amount(new BigDecimal("100.00")).build();
        when(service.create(any())).thenReturn(sample(1L, PaymentStatus.PENDING));

        mvc.perform(post("/api/billing").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_returns_400_when_invalid() throws Exception {
        mvc.perform(post("/api/billing").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_or_404() throws Exception {
        when(service.getById(1L)).thenReturn(sample(1L, PaymentStatus.PENDING));
        mvc.perform(get("/api/billing/1")).andExpect(status().isOk());

        doThrow(new BillNotFoundException(2L)).when(service).getById(2L);
        mvc.perform(get("/api/billing/2")).andExpect(status().isNotFound());
    }

    @Test
    void markPaid_returns_200() throws Exception {
        when(service.markPaid(1L)).thenReturn(sample(1L, PaymentStatus.PAID));
        mvc.perform(put("/api/billing/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));
    }

    @Test
    void getByPatient_returns_list() throws Exception {
        when(service.getByPatient(2L)).thenReturn(List.of(sample(1L, PaymentStatus.PENDING)));
        mvc.perform(get("/api/billing/patient/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
