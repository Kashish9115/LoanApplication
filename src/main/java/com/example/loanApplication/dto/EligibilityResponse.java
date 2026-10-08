package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityResponse {

    private Integer eligibilityId;

    private Integer customerId;

    private Integer cibilScore;

    private String cibilStatus;

    private Boolean eligible;

    private BigDecimal eligibleLoanAmount;

    private String rejectionReason;
}