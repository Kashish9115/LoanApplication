package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistoryResponseDTO {

    private Integer paymentId;

    private Integer loanAccountId;

    private BigDecimal paymentAmount;

    private String paymentStatus;

    private LocalDateTime paymentDate;

    private String paymentName;
}