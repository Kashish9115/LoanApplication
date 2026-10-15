
package com.example.loanApplication.dto;

import com.example.loanApplication.enumeration.DisbursementStatus;
import com.example.loanApplication.enumeration.LoanDealStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisbursementResponse {

    private Integer disbursementId;

    private Integer dealId;

    private Integer customerId;

    private BigDecimal disbursedAmount;

    private String bankName;

    private String bankAccountNumber;

    private String accountHolderName;

    private String ifscCode;

    private DisbursementStatus disbursementStatus;

    private LoanDealStatus dealStatus;

    // Date selected by the officer.
    private LocalDate plannedDisbursementDate;

    // Actual date and time when disbursement was completed.
    private LocalDateTime disbursedAt;

    private String message;
}
