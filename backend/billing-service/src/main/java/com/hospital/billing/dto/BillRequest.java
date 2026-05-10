package com.hospital.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillRequest {

    @NotNull
    private Long patientId;

    private Long appointmentId;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String description;
}
