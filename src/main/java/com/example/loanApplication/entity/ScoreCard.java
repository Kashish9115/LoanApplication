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
import java.time.LocalDateTime;

@Entity
@Table(name = "ScoreCards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScoreCardId")
    private Integer scoreCardId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "CurrentStatus", length = 100)
    private String currentStatus;

    @Column(name = "RejectionReason", length = 1000)
    private String rejectionReason;

    @Column(name = "AppliedDate")
    private LocalDateTime appliedDate;

    @Column(name = "CibilScore")
    private Integer cibilScore;

    @Column(name = "RiskCategory", length = 50)
    private String riskCategory;

    @Column(
            name = "EligibleLoanAmount",
            precision = 18,
            scale = 2
    )
    private BigDecimal eligibleLoanAmount;
}
