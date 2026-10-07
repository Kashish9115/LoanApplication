package com.example.loanApplication.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmiScheduleResponseDto {



    private Integer installmentNo;
    private LocalDate dueDate;
    private BigDecimal emi;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private String paymentStatus;




}
