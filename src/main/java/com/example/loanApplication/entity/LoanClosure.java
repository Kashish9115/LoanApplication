package com.example.loanApplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LoanClosures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClosureId")
    private Integer closureId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "ClosureType", length = 50)
    private String closureType;

    @Column(name = "FinalSettlementAmount", precision = 18, scale = 2)
    private BigDecimal finalSettlementAmount;

    @Column(name = "ClosureDate")
    private LocalDateTime closureDate;

    @Column(name = "ClosedBy")
    private Integer closedBy;

    @Column(name = "Remarks", length = 1000)
    private String remarks;

    @Column(name = "ClosureStatus", length = 50)
    private String closureStatus;
}