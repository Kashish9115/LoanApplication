package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterestAccrualRequestDTO {

    private Long loanAccountId;
    private Integer installmentNo;
    private BigDecimal interestAmount;
    private LocalDateTime accrualDate;
    private String status;

}
