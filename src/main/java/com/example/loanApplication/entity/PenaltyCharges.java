package com.example.loanApplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PenaltyCharges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyCharges {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PenaltyChargeId")
    private Integer penaltyChargeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmiScheduleId", nullable = false)
    private EmiSchedules emiSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
  //  private LoanAccount loanAccount;

    @Column(name = "PenaltyAmount", precision = 18, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(name = "Reason", length = 1000)
    private String reason;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}