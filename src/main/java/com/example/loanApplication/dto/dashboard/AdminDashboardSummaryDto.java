package com.example.loanApplication.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardSummaryDto {

    // --- 1. LOS KPI Cards (Origination) ---
    private Long totalApplications;

    private Long approvedApplications;

    private Long rejectedApplications;

    private Long pendingApplications;


    // --- 2. LMS KPI Cards (Portfolio Management) ---
    //total loan
    private Long totalLoansCount;

    // Active Loans
    private Long activeLoansCount;

    // Closed Loans
    private Long closedLoansCount;

    // Total Active Outstanding Value (₹)
    private BigDecimal totalPortfolioValue;


    // --- 3. Visual Charts ---
    private ApplicationFunnelDto conversionFunnel;

    private List<MonthlyTrendDto> monthlyApplicationTrends;

    private List<PortfolioBreakdownDto> portfolioBreakdown;

    private List<CollectionEfficiencyDto> collectionEfficiencyTrends;

}