package com.example.loanApplication.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentPaymentDto {

    private Integer paymentId;
    private LocalDate paymentDate;
    private String paymentType;
    private BigDecimal amount;
    private String status;
}

