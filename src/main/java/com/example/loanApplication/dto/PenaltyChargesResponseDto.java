package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyChargesResponseDto {

    private Long penaltyChargeId;      // Primary key generated in PenaltyCharges table
    private Long loanAccountId;
    private Long emiScheduleId;
    private BigDecimal penaltyAmount;  // e.g., ₹600.00 to ₹700.00 bounce fee
    private String reason;              // Detailed penalty reason displayed on invoice breakdown
    private String status;              // e.g., "APPLIED", "PAID", or "WAIVED"
    private LocalDateTime createdAt;
    private String responseMessage;     // e.g., "Penalty charge applied successfully"
}
