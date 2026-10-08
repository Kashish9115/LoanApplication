package com.example.loanApplication.dto;

import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.enumeration.LoanType;

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
public class SanctionResponse {

    private Integer sanctionId;

    private Integer dealId;

    private Integer customerId;

    private LoanType loanType;

    private BigDecimal approvedLoanAmount;

    private BigDecimal interestRate;

    private Integer tenureMonths;

    private BigDecimal emiAmount;

    private Integer emiDay;

    private LoanDealStatus dealStatus;

    private LocalDateTime createdAt;

    private String message;
}