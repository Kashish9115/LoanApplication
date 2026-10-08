package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreCardResponse {

    private Integer scoreCardId;

    private Integer customerId;

    private Integer cibilScore;

    private String cibilStatus;

    private String riskCategory;

    private BigDecimal eligibleLoanAmount;

    private String approvalType;

    private String currentStatus;

    private String rejectionReason;

    private LocalDateTime appliedDate;
}