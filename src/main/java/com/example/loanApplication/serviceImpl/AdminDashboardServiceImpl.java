package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.dto.dashboard.*;
import com.example.loanApplication.dto.report.CibilScoreRangeReportDto;
import com.example.loanApplication.enumeration.DisbursementStatus;
import com.example.loanApplication.enumeration.LoanAccountStatus;
import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.enumeration.ReviewStatus;
import com.example.loanApplication.repository.*;
import com.example.loanApplication.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final ScoreCardRepository scoreCardRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final EmiSchedulesRepo emiScheduleRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final DealReviewRepository dealReviewRepository;
    private final LoanDealRepository loanDealRepository;
    private final DisbursementRepository disbursementRepository;


    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryDto getDashboardSummary() {


       // 1. Fetch Overall LOS KPI Metrics
        Long totalApps = scoreCardRepository.count();
        Long approvedApps = scoreCardRepository.countByCurrentStatus("APPROVED");
        Long rejectedApps = scoreCardRepository.countByCurrentStatus("REJECTED");
        Long pendingApps = scoreCardRepository.countByCurrentStatus("PENDING");


        Long totalLoans = loanAccountRepository.count();
        Long activeLoans = loanAccountRepository.countByStatus(LoanAccountStatus.ACTIVE);
        Long closedLoans = loanAccountRepository.countByStatus(LoanAccountStatus.CLOSED);
        BigDecimal portfolioValue = loanAccountRepository.calculateTotalPortfolioValue(LoanAccountStatus.ACTIVE);


        Long kycVerified = kycDocumentRepository.countByVerificationStatus("APPROVED");
        Long reviewed = dealReviewRepository.countByStatus(ReviewStatus.APPROVED);
        Long dealApproved = loanDealRepository.countByDealStatus(LoanDealStatus.APPROVED);
        Long disbursed = disbursementRepository.countByStatus(DisbursementStatus.COMPLETED);

        ApplicationFunnelDto funnel = ApplicationFunnelDto.builder()
                .submittedCount(totalApps)
                .kycVerifiedCount(kycVerified != null ? kycVerified : 0L)
                .reviewedCount(reviewed != null ? reviewed : 0L)
                .approvedCount(dealApproved != null ? dealApproved : 0L)
                .disbursedCount(disbursed != null ? disbursed : 0L) .build();


        // 4\. Construct Monthly Application Trends (Line Chart)
        List<Object[]> rawTrends = scoreCardRepository.fetchMonthlyApplicationTrends();
        List<MonthlyTrendDto> monthlyTrends = new ArrayList<>();
        for (Object[] row : rawTrends) {
            Integer monthNum = (Integer) row[0];
            Long count = (Long) row[1];
            monthlyTrends.add(new MonthlyTrendDto(monthNum, Month.of(monthNum).name(), count));
        }

        // 5. Fetch Portfolio Breakdown (Donut Chart)
        List<PortfolioBreakdownDto> portfolioBreakdown = loanAccountRepository.fetchPortfolioBreakdown(LoanAccountStatus.ACTIVE);

        // 6\. Calculate Monthly Collection Efficiency (Bar Chart)
        List<Object[]> rawEfficiency = emiScheduleRepository.fetchCollectionEfficiencyByMonth();
        List<CollectionEfficiencyDto> collectionTrends = new ArrayList<>();
      
        for (Object[] row : rawEfficiency) {
            Integer monthNum = (Integer) row[0];

            BigDecimal billed = row[1] != null
                    ? new BigDecimal(row[1].toString())
                    : BigDecimal.ZERO;

            BigDecimal collected = row[2] != null
                    ? new BigDecimal(row[2].toString())
                    : BigDecimal.ZERO;

            Double efficiency = 0.0;

            if (billed.compareTo(BigDecimal.ZERO) > 0) {
                efficiency = collected
                        .divide(billed, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
            }

            collectionTrends.add(
                    new CollectionEfficiencyDto(
                            monthNum,
                            billed,
                            collected,
                            efficiency
                    )
            );
        }
        return AdminDashboardSummaryDto.builder()
                .totalApplications(totalApps)
                .approvedApplications(approvedApps)
                .rejectedApplications(rejectedApps)
                .pendingApplications(pendingApps)
                .totalLoansCount(totalLoans)
                .activeLoansCount(activeLoans)
                .closedLoansCount(closedLoans)
                .totalPortfolioValue(portfolioValue)
                .conversionFunnel(funnel)
                .monthlyApplicationTrends(monthlyTrends)
                .portfolioBreakdown(portfolioBreakdown)
                .collectionEfficiencyTrends(collectionTrends)
                .build();

    }



}
