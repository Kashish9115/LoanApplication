package com.example.loanApplication.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor

public class EmiScheduleResponseDto {




    private Integer emiScheduleId;
    private Integer installmentNo;

    private BigDecimal emi;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;

    private LocalDate dueDate;
    private String paymentStatus;
    private LocalDateTime paidDate;
    private String cancellationReason;


    }

