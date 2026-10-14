package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForeClosureLoanDto {

    private Integer loanAccountId;
    private String loanAccountNumber;
    private BigDecimal outstandingPrincipal;
    private LocalDateTime nextEmiDate;
}