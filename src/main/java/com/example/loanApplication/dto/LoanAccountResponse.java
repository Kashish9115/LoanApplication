package com.example.loanApplication.dto;

import com.example.loanApplication.enumeration.LoanAccountStatus;
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
public class LoanAccountResponse {

    private Integer loanAccountId;

    private Integer dealId;

    private Integer customerId;

    private String loanAccountNo;

    private LoanType loanType;

    private BigDecimal loanAmount;

    private BigDecimal outstandingAmount;

    private BigDecimal interestRate;

    private Integer tenureMonths;

    private BigDecimal emiAmount;

    private Integer emiDay;

    private LoanAccountStatus status;

    private LocalDateTime createdAt;

    private String message;
}