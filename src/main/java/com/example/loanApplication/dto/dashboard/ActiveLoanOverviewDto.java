package com.example.loanApplication.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActiveLoanOverviewDto {

    private Integer loanAccountId;
    private String loanAccountNo;
    private String loanType;
    private String status;
    private BigDecimal loanAmount;
    private LocalDate disbursedOn;
    private Integer tenureMonths;
    private BigDecimal interestRate;
    private BigDecimal emiAmount;
    private Integer emisPaidCount;
    private String emisPaidRatio;
    private BigDecimal outstandingAmount;
    private Double paidPercentage;
    private Integer remainingEmis;
}
