package com.example.loanApplication.entity;

import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.enumeration.LoanType;

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
import java.time.LocalDate;

@Entity
@Table(name = "LoanDeals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DealId")
    private Integer dealId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "LoanType", length = 100)
    private LoanType loanType;

    @Column(name = "LoanAmount", precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "InterestRate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "TenureMonths")
    private Integer tenureMonths;

    @Column(name = "EmiAmount", precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "BankName", length = 200)
    private String bankName;

    @Column(name = "BankAccountNumber", length = 50)
    private String bankAccountNumber;

    @Column(name = "AccountHolderName", length = 200)
    private String accountHolderName;

    @Column(name = "IFSCCode", length = 20)
    private String ifscCode;

    @Column(name = "EmiDay")
    private Integer emiDay;

    @Column(name = "PreferredDisbursementDate")
    private LocalDate preferredDisbursementDate;

    @Column(name = "ApprovedAmount", precision = 18, scale = 2)
    private BigDecimal approvedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "DealStatus", length = 50)
    private LoanDealStatus dealStatus;
}