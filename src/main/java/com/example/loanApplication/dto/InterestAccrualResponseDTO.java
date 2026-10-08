package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class InterestAccrualResponseDTO {

    private Long accrualId;            // Primary key generated in InterestAccruals table
    private Long loanAccountId;
    private Integer installmentNo;     // Corresponding EMI installment number
    private BigDecimal interestAmount; // Per-day accrued late interest amount
    private LocalDateTime accrualDate; // Date on which daily interest was calculated
    private String status;              // e.g., "APPLIED", "PAID", or "WAIVED"
    private String responseMessage;     // e.g., "Daily interest accrual logged successfully"


}
