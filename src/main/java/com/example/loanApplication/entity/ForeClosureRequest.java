package com.example.loanApplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ForeClosureRequests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForeClosureRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestId")
    private Integer requestId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "ForeClosureType", length = 50)
    private String foreClosureType;

    @Column(name = "Months")
    private Integer months;

    @Column(name = "ForeClosureAmount", precision = 18, scale = 2)
    private BigDecimal foreClosureAmount;

    @Column(name = "PartialAmount", precision = 18, scale = 2)
    private BigDecimal partialAmount;

    @Column(name = "RequestedDate")
    private LocalDateTime requestedDate;

    @Column(name = "ExpectedClosureDate")
    private LocalDateTime expectedClosureDate;

    @Column(name = "Reason", length = 1000)
    private String reason;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "ApprovedDate")
    private LocalDateTime approvedDate;

    @Column(name = "IsPaid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "PaidDate")
    private LocalDateTime paidDate;

    @Column(name = "ClosedBy")
    private Integer closedBy;
}
