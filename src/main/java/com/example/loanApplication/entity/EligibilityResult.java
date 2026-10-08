package com.example.loanApplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "EligibilityResults")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EligibilityId")
    private Integer eligibilityId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "CibilScore")
    private Integer cibilScore;

    @Column(name = "IsEligible", nullable = false)
    private Boolean isEligible;

    @Column(name = "LoanAmount", precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "RejectionReason", length = 1000)
    private String rejectionReason;
}