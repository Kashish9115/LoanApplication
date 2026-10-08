package com.example.loanApplication.dto;

import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.enumeration.LoanType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDealResponse {

    private Integer dealId;

    private Integer customerId;

    private LoanType loanType;

    /*
     * Amount requested by customer.
     */
    private BigDecimal requestedLoanAmount;

    /*
     * Maximum amount customer is eligible for.
     */
    private BigDecimal eligibleLoanAmount;

    /*
     * Final approved amount.
     *
     * For AUTO_APPROVED:
     * approvedAmount = requestedLoanAmount
     *
     * For MANUAL_REVIEW:
     * this can remain null until officer approves.
     */
    private BigDecimal approvedAmount;

    private Integer tenureMonths;

    private Integer emiDay;

    private String bankName;

    private String bankAccountNumber;

    private String accountHolderName;

    private String ifscCode;

    private LocalDate preferredDisbursementDate;

    private Integer cibilScore;

    private String cibilStatus;

    private LoanDealStatus dealStatus;
}