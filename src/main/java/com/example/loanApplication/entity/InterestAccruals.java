package com.example.loanApplication.entity;

import jakarta.persistence.*;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class InterestAccruals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccrualId")
    private Integer accrualId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "InterestAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "AccrualDate")
    private LocalDateTime accrualDate;

    @Column(name = "InstallmentNo")
    private Integer installmentNo;

    @Column(name = "Status", length = 50)
    private String status;
}