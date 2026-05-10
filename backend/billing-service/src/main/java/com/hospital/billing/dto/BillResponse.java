package com.hospital.billing.dto;

import com.hospital.billing.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {
    private Long id;
    private Long patientId;
    private Long appointmentId;
    private BigDecimal amount;
    private String description;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
}
