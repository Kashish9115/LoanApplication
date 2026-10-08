package com.example.loanApplication.entity;


import com.example.loanApplication.enumeration.EligibilityStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Entity
@Table(name = "EligibilityResults")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private Boolean isEligible = false;

    @Column(name = "LoanAmount", precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "RejectionReason", length = 1000)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private EligibilityStatusEnum status;
}