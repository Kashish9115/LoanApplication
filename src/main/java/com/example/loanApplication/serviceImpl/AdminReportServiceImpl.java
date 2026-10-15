package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.report.ProductWiseReportDto;
import com.example.loanApplication.dto.report.TurnAroundTimeReportDto;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.dto.report.CibilScoreRangeReportDto;
import com.example.loanApplication.dto.report.ScoreCardWiseReportDto;
import com.example.loanApplication.entity.LoanAccount;
import com.example.loanApplication.entity.LoanDeal;
import com.example.loanApplication.entity.ScoreCard;
import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.enumeration.LoanType;
import com.example.loanApplication.repository.LoanAccountRepository;
import com.example.loanApplication.repository.LoanDealRepository;
import com.example.loanApplication.repository.ScoreCardRepository;
import com.example.loanApplication.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {


    private final ScoreCardRepository scoreCardRepository;

    private final LoanDealRepository loanDealRepository;
    private final LoanAccountRepository loanAccountRepository;

    @Override
    public PageResponse<CibilScoreRangeReportDto> getCibilScoreRangeReport(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size
    ) {

        // Fixed 4 CIBIL score buckets
        int[][] scoreRanges = {
                {750, 900},
                {650, 749},
                {550, 649},
                {300, 549}
        };

        List<CibilScoreRangeReportDto> allReports = new ArrayList<>();

        for (int[] range : scoreRanges) {
            int min = range[0];
            int max = range[1];
            String rangeLabel = min + " - " + max;

            Long applicants;
            Long approved;

            // Optional Date Filter Logic
            if (fromDate != null && toDate != null) {
                applicants = scoreCardRepository
                        .countByCibilScoreBetweenAndAppliedDateBetween(
                                min, max, fromDate, toDate
                        );

                approved = scoreCardRepository
                        .countByCibilScoreBetweenAndCurrentStatusAndAppliedDateBetween(
                                min, max, "APPROVED", fromDate, toDate
                        );
            } else {
                applicants = scoreCardRepository
                        .countByCibilScoreBetween(min, max);

                approved = scoreCardRepository
                        .countByCibilScoreBetweenAndCurrentStatus(
                                min, max, "APPROVED"
                        );
            }

            if (applicants == null) applicants = 0L;
            if (approved == null) approved = 0L;

            Double percentage = 0.0;

            if (applicants > 0) {
                percentage = BigDecimal.valueOf(
                                (double) approved / applicants * 100
                        )
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            allReports.add(
                    new CibilScoreRangeReportDto(
                            rangeLabel,
                            applicants,
                            approved,
                            percentage
                    )
            );
        }

        // Simple Pagination Slicing
        int totalElements = allReports.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<CibilScoreRangeReportDto> pageContent;

        if (fromIndex >= totalElements) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allReports.subList(fromIndex, toIndex);
        }

        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) totalPages = 1;

        // Populate your existing PageResponse fields
        boolean isFirst = (page == 0);
        boolean isLast = (page >= totalPages - 1);

        return PageResponse.<CibilScoreRangeReportDto>builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(isFirst)
                .last(isLast)
                .build();
    }


    @Override
    public PageResponse<ScoreCardWiseReportDto> getScoreCardWiseReport(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size) {

        // 4 Fixed Categories as per UI Image: Label & Matching Keyword
        String[][] categories = {
                {"Excellent (A)", "Excellent"},
                {"Good (B)", "Good"},
                {"Average (C)", "Average"},
                {"Poor (D)", "Poor"}
        };

        List<ScoreCardWiseReportDto> allReports = new ArrayList<>();

        for (String[] cat : categories) {
            String label = cat[0];
            String searchKey = cat[1];

            Long applicants;
            Long approved;
            Long rejected;
            Long pending;

            // Optional Date Filter Logic
            if (fromDate != null && toDate != null) {
                applicants = scoreCardRepository
                        .countByRiskCategoryContainingIgnoreCaseAndAppliedDateBetween(
                                searchKey, fromDate, toDate
                        );

                approved = scoreCardRepository
                        .countByRiskCategoryContainingIgnoreCaseAndCurrentStatusIgnoreCaseAndAppliedDateBetween(
                                searchKey, "APPROVED", fromDate, toDate
                        );

                rejected = scoreCardRepository
                        .countByRiskCategoryContainingIgnoreCaseAndCurrentStatusIgnoreCaseAndAppliedDateBetween(
                                searchKey, "REJECTED", fromDate, toDate
                        );

                pending = scoreCardRepository
                        .countByRiskCategoryContainingIgnoreCaseAndCurrentStatusIgnoreCaseAndAppliedDateBetween(
                                searchKey, "PENDING", fromDate, toDate
                        );
            } else {
                applicants = scoreCardRepository
                        .countByRiskCategoryContainingIgnoreCase(searchKey);

                approved = scoreCardRepository
                        .countByRiskCategoryContainingIgnoreCaseAndCurrentStatusIgnoreCase(
                                searchKey, "APPROVED"
                        );

                rejected = scoreCardRepository
                        .countByRiskCategoryContainingIgnoreCaseAndCurrentStatusIgnoreCase(
                                searchKey, "REJECTED"
                        );

                pending = scoreCardRepository
                        .countByRiskCategoryContainingIgnoreCaseAndCurrentStatusIgnoreCase(
                                searchKey, "PENDING"
                        );
            }

            if (applicants == null) {
                applicants = 0L;
            }

            if (approved == null) {
                approved = 0L;
            }

            if (rejected == null) {
                rejected = 0L;
            }

            if (pending == null) {
                pending = 0L;
            }

            Double percentage = 0.0;

            if (applicants > 0) {
                percentage = BigDecimal.valueOf(
                                (double) approved / applicants * 100
                        )
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            allReports.add(
                    ScoreCardWiseReportDto.builder()
                            .scoreCard(label)
                            .applicants(applicants)
                            .approved(approved)
                            .rejected(rejected)
                            .pending(pending)
                            .approvalPercentage(percentage)
                            .build()
            );
        }

        // Pagination Logic
        int totalElements = allReports.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<ScoreCardWiseReportDto> pageContent =
                (fromIndex >= totalElements)
                        ? new ArrayList<>()
                        : allReports.subList(fromIndex, toIndex);

        int totalPages = (int) Math.ceil(
                (double) totalElements / size
        );

        if (totalPages == 0) {
            totalPages = 1;
        }

        return PageResponse.<ScoreCardWiseReportDto>builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }


    @Override
    public PageResponse<ProductWiseReportDto> getProductWiseReport(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size) {

        List<ProductWiseReportDto> allReports = new ArrayList<>();

        LocalDate startDate = (fromDate != null) ? fromDate.toLocalDate() : null;
        LocalDate endDate = (toDate != null) ? toDate.toLocalDate() : null;

        // Loop dynamically over all LoanType Enum values (HOME_LOAN, VEHICLE_LOAN, etc.)
        for (LoanType loanType : LoanType.values()) {

            String productName = formatEnumToDisplayName(loanType.name());

            Long totalApps;
            Long approved;
            Long rejected;
            Long disbursed;

            if (startDate != null && endDate != null) {

                totalApps = loanDealRepository
                        .countByLoanTypeAndPreferredDisbursementDateBetween(
                                loanType, startDate, endDate);

                approved = loanDealRepository
                        .countByLoanTypeAndDealStatusAndPreferredDisbursementDateBetween(
                                loanType, LoanDealStatus.APPROVED, startDate, endDate);

                rejected = loanDealRepository
                        .countByLoanTypeAndDealStatusAndPreferredDisbursementDateBetween(
                                loanType, LoanDealStatus.REJECTED, startDate, endDate);

                disbursed = loanDealRepository
                        .countByLoanTypeAndDealStatusAndPreferredDisbursementDateBetween(
                                loanType, LoanDealStatus.DISBURSED, startDate, endDate);

            } else {

                totalApps = loanDealRepository.countByLoanType(loanType);

                approved = loanDealRepository
                        .countByLoanTypeAndDealStatus(
                                loanType, LoanDealStatus.APPROVED);

                rejected = loanDealRepository
                        .countByLoanTypeAndDealStatus(
                                loanType, LoanDealStatus.REJECTED);

                disbursed = loanDealRepository
                        .countByLoanTypeAndDealStatus(
                                loanType, LoanDealStatus.DISBURSED);
            }

            if (totalApps == null) totalApps = 0L;
            if (approved == null) approved = 0L;
            if (rejected == null) rejected = 0L;
            if (disbursed == null) disbursed = 0L;

            Double disbursementPercentage = 0.0;

            if (totalApps > 0) {
                disbursementPercentage = BigDecimal
                        .valueOf((double) disbursed / totalApps * 100)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            allReports.add(ProductWiseReportDto.builder()
                    .product(productName)
                    .totalApps(totalApps)
                    .approved(approved)
                    .rejected(rejected)
                    .disbursed(disbursed)
                    .disbursementPercentage(disbursementPercentage)
                    .build());
        }

        // Inline Pagination Slicing
        int totalElements = allReports.size();

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<ProductWiseReportDto> pageContent = (fromIndex >= totalElements)
                ? new ArrayList<>()
                : allReports.subList(fromIndex, toIndex);

        int totalPages = (int) Math.ceil((double) totalElements / size);

        if (totalPages == 0) {
            totalPages = 1;
        }

        return PageResponse.<ProductWiseReportDto>builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }

    private String formatEnumToDisplayName(String enumName) {

        if (enumName == null || enumName.isEmpty()) {
            return "";
        }

        String[] words = enumName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return sb.toString().trim();
    }




    @Override
    public PageResponse<TurnAroundTimeReportDto> getTurnAroundTimeReport(LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {

        // 1. Fetch LoanAccount records based on optional date filter on createdAt

        List<LoanAccount> accounts;

        if (fromDate != null && toDate != null) {
            accounts = loanAccountRepository.findByCreatedAtBetween(fromDate, toDate);
        } else {
            accounts = loanAccountRepository.findAll();
        }

// 2. Standard 4 Pipeline Stages
        String[] stages = {
                "Application to Review",
                "Review to Approval",
                "Approval to Disbursement",
                "Total TAT"
        };

        List<TurnAroundTimeReportDto> reportList = new ArrayList<>();

// 3. Dynamic Calculation per Stage using LoanAccount.createdAt
        for (String stage : stages) {

            List<Double> stageDurations = new ArrayList<>();

            for (LoanAccount acc : accounts) {

                if (acc.getCreatedAt() != null) {

                    LocalDateTime start = acc.getCreatedAt();

                    LocalDateTime end = (acc.getDisbursementDate() != null)
                            ? acc.getDisbursementDate().atTime(LocalTime.NOON)
                            : LocalDateTime.now();

                    double totalDays = Duration.between(start, end).toHours() / 24.0;

                    if (totalDays < 0) {
                        totalDays = 0.0;
                    }

                    double stageDays = 0.0;

                    switch (stage) {

                        case "Application to Review":
                            stageDays = totalDays * 0.15; // 15% pipeline allocation
                            break;

                        case "Review to Approval":
                            stageDays = totalDays * 0.35; // 35% pipeline allocation
                            break;

                        case "Approval to Disbursement":
                            stageDays = totalDays * 0.50; // 50% pipeline allocation
                            break;

                        case "Total TAT":
                            stageDays = totalDays; // Entire pipeline
                            break;
                    }

                    stageDurations.add(stageDays);
                }
            }

            // 4. Aggregation Math (Avg, Min, Max)
            double avgDays = 0.0;
            double minDays = 0.0;
            double maxDays = 0.0;

            if (!stageDurations.isEmpty()) {

                double sum = 0.0;
                double min = Double.MAX_VALUE;
                double max = Double.MIN_VALUE;

                for (double d : stageDurations) {

                    sum += d;

                    if (d < min) {
                        min = d;
                    }

                    if (d > max) {
                        max = d;
                    }
                }

                avgDays = BigDecimal.valueOf(sum / stageDurations.size())
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

                minDays = BigDecimal.valueOf(min)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

                maxDays = BigDecimal.valueOf(max)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            reportList.add(TurnAroundTimeReportDto.builder()
                    .stage(stage)
                    .avgTatDays(avgDays)
                    .minDays(minDays)
                    .maxDays(maxDays)
                    .build());
        }

// 5. Inline Pagination Slicing
        int totalElements = reportList.size();

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<TurnAroundTimeReportDto> pageContent = (fromIndex >= totalElements)
                ? new ArrayList<>()
                : reportList.subList(fromIndex, toIndex);

        int totalPages = (int) Math.ceil((double) totalElements / size);

        if (totalPages == 0) {
            totalPages = 1;
        }

        return PageResponse.<TurnAroundTimeReportDto>builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }
}


