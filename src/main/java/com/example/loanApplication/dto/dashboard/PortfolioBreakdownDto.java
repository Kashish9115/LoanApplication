package com.example.loanApplication.dto.dashboard;

import com.example.loanApplication.enumeration.LoanType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioBreakdownDto {

    private LoanType loanType;// "HOME_LOAN", "VEHICLE_LOAN", "BUSINESS_LOAN"

    private BigDecimal totalOutstanding;

    private Long loanCount;
}