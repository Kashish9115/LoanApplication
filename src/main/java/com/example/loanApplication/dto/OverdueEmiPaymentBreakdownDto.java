package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueEmiPaymentBreakdownDto {

    private Integer loanAccountId;

    private List<EmiBreakDownDto> overdueEmis;

    private Integer totalOverdueEmis;

    private BigDecimal totalBaseEmi;

    private BigDecimal totalBounceCharge;

    private BigDecimal totalLateFee;

    private BigDecimal totalPayableAmount;
}