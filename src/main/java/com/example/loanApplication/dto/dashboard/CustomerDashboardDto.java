package com.example.loanApplication.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDashboardDto {

    // 1. Header Profile Info
    private Integer customerId;
    private String customerCode; // e.g., "CUST10045"
    private String customerName; // e.g., "Rahul Sharma"
    private String verificationStatus; // e.g., "Verified"

    // 2. Top 5 KPI Summary Cards
    private Long totalApplications;
    private Long activeLoans;
    private Long emisPaid;
    private BigDecimal upcomingEmiAmount;
    private LocalDate upcomingEmiDueDate;
    private BigDecimal totalAmountPaid;

    // 3. Active Loan Application Progress Stepper
    private ApplicationProgressDto applicationProgress;

    // 4. Active Loan Overview Card
    private ActiveLoanOverviewDto activeLoanOverview;

    // 5. EMI Breakdown (Donut Chart Data)
    private EmiSummaryDto emiSummary;

    // 6. Recent Payment History
    private List<RecentPaymentDto> recentPayments;

    // 7. Documents Status Checklist
    private List<DocumentStatusDto> documents;

    // 8. Recent Activity Timeline
    private List<ActivityTimelineDto> recentActivities;
}