package com.example.loanApplication.entity;

import com.example.loanApplication.enumeration.LoanAccountStatus;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LoanAccounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LoanAccountId")
    private Integer loanAccountId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "DealId", nullable = false)
    private Integer dealId;

    @Column(name = "LoanAccountNo", nullable = false, unique = true, length = 100)
    private String loanAccountNo;

    @Column(name = "LoanAmount", precision = 18, scale = 2, nullable = false)
    private BigDecimal loanAmount;

    @Column(name = "OutstandingPrincipal", precision = 18, scale = 2)
    private BigDecimal outstandingAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "LoanStatus", length = 50)
    private LoanAccountStatus status;

    @Column(name = "InterestRate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "TenureMonths")
    private Integer tenureMonths;

    @Column(name = "EmiAmount", precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "EmiDay")
    private Integer emiDay;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;







    @Column(name = "DisbursementDate")
    private LocalDate disbursementDate;
}