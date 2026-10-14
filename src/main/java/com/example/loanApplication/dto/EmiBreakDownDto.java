package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmiBreakDownDto {


    private Integer loanAccountId;
    private Integer installmentNo;
    private LocalDate dueDate;
  //  private BigDecimal emi;
    private String paymentStatus;
    private BigDecimal baseEmi;
    private BigDecimal bounceCharge;
    private BigDecimal lateFeePerDay;
    private BigDecimal totalPayableAmount;
    private Integer emiScheduleId;
}
