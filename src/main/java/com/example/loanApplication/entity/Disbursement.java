package com.example.loanApplication.entity;

import com.example.loanApplication.enumeration.DisbursementStatus;

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
@Table(name = "Disbursements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DisbursementId")
    private Integer disbursementId;

    @Column(name = "DealId", nullable = false)
    private Integer dealId;

    @Column(name = "DisburseAmount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "BankAccountNumber", length = 50)
    private String bankAccountNumber;

    @Column(name = "IFSCCode", length = 20)
    private String ifscCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50)
    private DisbursementStatus status;

    @Column(name = "DisbursedAt")
    private LocalDateTime disbursedAt;
}