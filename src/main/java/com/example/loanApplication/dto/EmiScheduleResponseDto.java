package com.example.loanApplication.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmiScheduleResponseDto {


    private Integer emiScheduleId;
    private Integer installmentNo;
    private LocalDate dueDate;
    private Integer loanAccountId;
    private BigDecimal emi;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private String paymentStatus;
    private LocalDateTime paidDate;
    private String cancellationReason;



}
