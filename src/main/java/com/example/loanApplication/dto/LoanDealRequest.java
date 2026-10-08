package com.example.loanApplication.dto;

import com.example.loanApplication.enumeration.LoanType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
public class LoanDealRequest {

    @NotNull(message = "Customer ID is required")
    private Integer customerId;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Requested loan amount is required")
    @Positive(message = "Requested loan amount must be greater than zero")
    private BigDecimal requestedLoanAmount;

    @NotNull(message = "Loan tenure is required")
    @Min(value = 1, message = "Loan tenure must be at least 1 month")
    private Integer tenureMonths;

    @NotNull(message = "Preferred EMI day is required")
    private Integer emiDay;

    @NotBlank(message = "Bank name is required")
    @Size(max = 200, message = "Bank name cannot exceed 200 characters")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    @Size(max = 50, message = "Bank account number cannot exceed 50 characters")
    private String bankAccountNumber;

    @NotBlank(message = "Account holder name is required")
    @Size(max = 200, message = "Account holder name cannot exceed 200 characters")
    private String accountHolderName;

    @NotBlank(message = "IFSC code is required")
    @Size(max = 20, message = "IFSC code cannot exceed 20 characters")
    private String ifscCode;

    @NotNull(message = "Preferred disbursement date is required")
    private LocalDate preferredDisbursementDate;
}