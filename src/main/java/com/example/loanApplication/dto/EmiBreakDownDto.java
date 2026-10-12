package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EmiBreakDownDto {

    private Integer customerId;
    private Integer installmentNo;
    private LocalDate dueDate;
    private BigDecimal emi;
    private String paymentStatus;


}
