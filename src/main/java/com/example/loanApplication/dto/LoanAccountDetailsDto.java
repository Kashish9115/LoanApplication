package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountDetailsDto {

    private Integer loanAccountId;
    private  Integer  customerId;
    private String loanAccountNo;
    private BigDecimal outstandingAmount;
    private LocalDate nextEmiDate;

}