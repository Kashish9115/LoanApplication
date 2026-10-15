package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.dashboard.*;
import com.example.loanApplication.entity.*;
import com.example.loanApplication.enumeration.LoanAccountStatus;
import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.repository.*;
import com.example.loanApplication.service.CustomerDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerDashboardServiceImpl implements CustomerDashboardService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanDealRepository loanDealRepository;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Autowired
    private EmiSchedulesRepo emiSchedulesRepository;

    @Autowired
    private LoanPaymentRepository loanPaymentsRepository;

    @Autowired
    private KycDocumentRepository kycDocumentRepository;

    // Dynamic Date & Time Formatters
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a");

    @Override
    public CustomerDashboardDto getCustomerDashboard(Integer customerId) {

        // 1. Fetch Customer Info from Database
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException(
                        "Customer not found with ID: " + customerId));

        String customerCode =
                "CUST" + String.format("%05d", customer.getCustomerId());

        String fullName =
                (customer.getFirstName() != null ? customer.getFirstName() : "")
                        + " "
                        + (customer.getLastName() != null ? customer.getLastName() : "");

        String verificationBadge =
                Boolean.TRUE.equals(customer.getIsEmailVerified())
                        ? "Verified"
                        : "Pending";

        // 2. Fetch Customer Deals & Active Loan Account
        List<LoanDeal> deals = loanDealRepository.findByCustomerIdOrderByDealIdDesc(customerId);

        Long totalApps = (long) deals.size();

        List<LoanAccount> activeAccounts =
                loanAccountRepository.findByCustomerIdAndStatus(
                        customerId, LoanAccountStatus.ACTIVE);

        Long activeLoansCount = (long) activeAccounts.size();

        LoanAccount activeLoan =
                activeAccounts.isEmpty() ? null : activeAccounts.get(0);

        // Fetch corresponding LoanDeal for the Active Loan
        LoanDeal activeLoanDeal = null;

        if (activeLoan != null && activeLoan.getDealId() != null) {
            activeLoanDeal = loanDealRepository
                    .findById(activeLoan.getDealId())
                    .orElse(null);
        }

        // 3. Compute Dynamic KPI Metrics
        Long emisPaidCount = 0L;
        BigDecimal upcomingEmiAmount = BigDecimal.ZERO;
        LocalDate upcomingDueDate = null;
        BigDecimal totalAmountPaid = BigDecimal.ZERO;

        if (activeLoan != null) {

            emisPaidCount =
                    emiSchedulesRepository
                            .countByLoanAccount_LoanAccountIdAndPaymentStatus(
                                    activeLoan.getLoanAccountId(), "PAID");

            Optional<EmiSchedules> upcomingEmiOpt =
                    emiSchedulesRepository
                            .findFirstByLoanAccount_LoanAccountIdAndPaymentStatusOrderByDueDateAsc(
                                    activeLoan.getLoanAccountId(), "PENDING");

            if (upcomingEmiOpt.isPresent()) {
                upcomingEmiAmount = upcomingEmiOpt.get().getEmi();
                upcomingDueDate = upcomingEmiOpt.get().getDueDate();
            }

            if (activeLoan.getTotalPaidAmount() != null) {
                totalAmountPaid = activeLoan.getTotalPaidAmount();
            }
        }

        // 4. Dynamic Application Progress Stepper Tracker
        ApplicationProgressDto progressDto = null;

        if (!deals.isEmpty()) {
            LoanDeal latestDeal = deals.get(deals.size() - 1);
            progressDto = buildApplicationProgress(latestDeal);
        }

        // 5. Dynamic Active Loan Overview Card
        ActiveLoanOverviewDto overviewDto = null;

        if (activeLoan != null) {
            overviewDto = buildActiveLoanOverview(
                    activeLoan, activeLoanDeal, emisPaidCount);
        }

        // 6. Dynamic EMI Breakdown / Donut Chart
        EmiSummaryDto emiSummaryDto = null;

        if (activeLoan != null) {
            emiSummaryDto = buildEmiSummary(
                    activeLoan,
                    emisPaidCount,
                    upcomingEmiAmount,
                    upcomingDueDate
            );
        }

        // 7. Dynamic Recent Payment History (Top 5)
        List<RecentPaymentDto> recentPayments = new ArrayList<>();

        if (activeLoan != null) {

            List<LoanPayments> payments =
                    loanPaymentsRepository
                            .findTop5ByLoanAccountIdOrderByPaymentDateDesc(
                                    activeLoan.getLoanAccountId());

            for (LoanPayments p : payments) {
                recentPayments.add(
                        RecentPaymentDto.builder()
                                .paymentId(p.getPaymentId())
                                .paymentDate(
                                        p.getPaymentDate() != null
                                                ? p.getPaymentDate().toLocalDate()
                                                : null)
                                .paymentType(
                                        p.getPaymentName() != null
                                                ? p.getPaymentName()
                                                : "EMI Payment")
                                .amount(p.getPaymentAmount())
                                .status(
                                        p.getPaymentStatus() != null
                                                ? p.getPaymentStatus()
                                                : "SUCCESS")
                                .build()
                );
            }
        }

        // 8. Dynamic Documents Checklist
        List<DocumentStatusDto> documentList = new ArrayList<>();

        List<KycDocument> kycDocs =
                kycDocumentRepository.findByCustomerId(customerId);

        for (KycDocument doc : kycDocs) {
            documentList.add(
                    DocumentStatusDto.builder()
                            .documentId(doc.getDocumentId())
                            .documentType(
                                    formatDynamicDocType(doc.getDocumentType()))
                            .status(
                                    doc.getVerificationStatus() != null
                                            ? doc.getVerificationStatus()
                                            : "Pending")
                            .build()
            );
        }

        // 9. Dynamic Activity Timeline Feed
        List<ActivityTimelineDto> activities =
                buildDynamicActivities(deals, activeLoan);

        // --- MASTER DTO ASSEMBLY ---
        return CustomerDashboardDto.builder()
                .customerId(customer.getCustomerId())
                .customerCode(customerCode)
                .customerName(fullName)
                .verificationStatus(verificationBadge)
                .totalApplications(totalApps)
                .activeLoans(activeLoansCount)
                .emisPaid(emisPaidCount)
                .upcomingEmiAmount(upcomingEmiAmount)
                .upcomingEmiDueDate(upcomingDueDate)
                .totalAmountPaid(totalAmountPaid)
                .applicationProgress(progressDto)
                .activeLoanOverview(overviewDto)
                .emiSummary(emiSummaryDto)
                .recentPayments(recentPayments)
                .documents(documentList)
                .recentActivities(activities)
                .build();
    }

    // --- DYNAMIC HELPER METHODS ---

    private ApplicationProgressDto buildApplicationProgress(LoanDeal deal) {

        String appId = "APP" + String.format("%05d", deal.getDealId());

        LoanDealStatus status = deal.getDealStatus();

        String statusStr = (status != null) ? status.name() : "SUBMITTED";

        int activeStep = 1;
        String bannerMsg;

        BigDecimal amount =
                (deal.getApprovedAmount() != null)
                        ? deal.getApprovedAmount()
                        : deal.getLoanAmount();

        String formattedAmount =
                (amount != null) ? "₹ " + amount : "";

        if (status != null) {
            switch (status) {

                case SUBMITTED:
                    activeStep = 1;
                    bannerMsg = "Application " + appId
                            + " submitted successfully. Awaiting document verification.";
                    break;

                case MANUAL_REVIEW:
                case AUTO_APPROVED:
                    activeStep = 3;
                    bannerMsg = "Your application " + appId
                            + " is undergoing credit underwriting and review.";
                    break;

                case APPROVED:
                case SANCTIONED:
                    activeStep = 4;
                    bannerMsg = "Congratulations! Loan application " + appId
                            + " sanctioned for " + formattedAmount + ".";
                    break;

                case DISBURSEMENT_PENDING:
                    activeStep = 5;
                    bannerMsg = "Sanction accepted. Disbursement payout of "
                            + formattedAmount + " is in progress.";
                    break;

                case DISBURSED:
                    activeStep = 5;
                    bannerMsg = "Loan amount of " + formattedAmount
                            + " has been credited to your bank account.";
                    break;

                case REJECTED:
                    activeStep = 1;
                    bannerMsg = "Application " + appId
                            + " declined based on credit policy criteria.";
                    break;

                default:
                    activeStep = 1;
                    bannerMsg = "Application " + appId
                            + " status: " + statusStr;
                    break;
            }
        } else {
            bannerMsg = "Application " + appId + " received.";
        }

        List<StepperStepDto> steps = new ArrayList<>();

        steps.add(new StepperStepDto(
                "Application Submitted",
                activeStep >= 1 ? "Completed" : "Pending",
                true,
                activeStep == 1
        ));

        steps.add(new StepperStepDto(
                "KYC & CIBIL Check",
                activeStep >= 2
                        ? "Completed"
                        : (activeStep == 1 ? "In-Progress" : "Pending"),
                activeStep >= 2,
                activeStep == 2
        ));

        steps.add(new StepperStepDto(
                "Underwriting & Review",
                activeStep >= 3 ? "Completed" : "Pending",
                activeStep >= 3,
                activeStep == 3
        ));

        steps.add(new StepperStepDto(
                "Loan Sanctioned",
                activeStep >= 4 ? "Completed" : "Pending",
                activeStep >= 4,
                activeStep == 4
        ));

        steps.add(new StepperStepDto(
                "Amount Disbursed",
                activeStep >= 5 ? "Completed" : "Pending",
                activeStep >= 5,
                activeStep == 5
        ));

        steps.add(new StepperStepDto(
                "Loan Active",
                activeStep >= 5 ? "Active" : "Pending",
                activeStep >= 5,
                activeStep == 6
        ));

        return ApplicationProgressDto.builder()
                .dealId(deal.getDealId())
                .applicationId(appId)
                .dealStatus(statusStr)
                .activeStep(activeStep)
                .bannerMessage(bannerMsg)
                .loanAmount(deal.getLoanAmount())
                .steps(steps)
                .build();
    }

    private ActiveLoanOverviewDto buildActiveLoanOverview(
            LoanAccount acc, LoanDeal deal, Long emisPaidCount) {

        int totalTenure =
                (acc.getTenureMonths() != null)
                        ? acc.getTenureMonths()
                        : 0;

        int remaining = Math.max(
                0, totalTenure - emisPaidCount.intValue());

        double paidPct = 0.0;

        if (totalTenure > 0) {
            paidPct = BigDecimal.valueOf(
                            (double) emisPaidCount / totalTenure * 100)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        // Dynamic Loan Type from Enum or Account Number
        String dynamicLoanType = "Loan Account";

        if (deal != null && deal.getLoanType() != null) {
            dynamicLoanType =
                    deal.getLoanType().name().replace("_", " ");
        }

        String accountNoStr =
                (acc.getLoanAccountNo() != null
                        && !acc.getLoanAccountNo().isEmpty())
                        ? acc.getLoanAccountNo()
                        : "LN" + String.format("%05d", acc.getLoanAccountId());

        String statusStr =
                (acc.getStatus() != null)
                        ? acc.getStatus().name()
                        : "ACTIVE";

        return ActiveLoanOverviewDto.builder()
                .loanAccountId(acc.getLoanAccountId())
                .loanAccountNo(accountNoStr)
                .loanType(dynamicLoanType)
                .status(statusStr)
                .loanAmount(acc.getLoanAmount())
                .disbursedOn(acc.getDisbursementDate())
                .tenureMonths(totalTenure)
                .interestRate(acc.getInterestRate())
                .emiAmount(acc.getEmiAmount())
                .emisPaidCount(emisPaidCount.intValue())
                .emisPaidRatio(emisPaidCount + " / " + totalTenure)
                .outstandingAmount(acc.getOutstandingAmount())
                .paidPercentage(paidPct)
                .remainingEmis(remaining)
                .build();
    }

    private EmiSummaryDto buildEmiSummary(
            LoanAccount acc,
            Long emisPaid,
            BigDecimal upcomingEmi,
            LocalDate nextDueDate) {

        int total =
                (acc.getTenureMonths() != null)
                        ? acc.getTenureMonths()
                        : 0;

        int remaining = Math.max(
                0, total - emisPaid.intValue() - 1);

        BigDecimal paidAmount =
                (acc.getTotalPaidAmount() != null)
                        ? acc.getTotalPaidAmount()
                        : BigDecimal.ZERO;

        return EmiSummaryDto.builder()
                .totalEmis(total)
                .paidEmisCount(emisPaid.intValue())
                .paidEmisAmount(paidAmount)
                .upcomingEmisCount(1)
                .upcomingEmiAmount(upcomingEmi)
                .nextEmiDueDate(nextDueDate)
                .remainingEmisCount(remaining)
                .remainingAmount(acc.getOutstandingAmount())
                .build();
    }

    private List<ActivityTimelineDto> buildDynamicActivities(
            List<LoanDeal> deals, LoanAccount activeLoan) {

        List<ActivityTimelineDto> list = new ArrayList<>();

        // 1. Dynamic Disbursement Activity Entry
        if (activeLoan != null
                && activeLoan.getDisbursementDate() != null) {

            LocalDateTime disDatetime =
                    activeLoan.getDisbursementDate().atTime(10, 0);

            list.add(
                    ActivityTimelineDto.builder()
                            .activityDate(
                                    activeLoan.getDisbursementDate()
                                            .format(DATE_FORMATTER))
                            .activityTime(
                                    disDatetime.format(TIME_FORMATTER))
                            .title("Loan Disbursed")
                            .description(
                                    "Amount ₹ " + activeLoan.getLoanAmount()
                                            + " disbursed for Account "
                                            + activeLoan.getLoanAccountNo())
                            .build()
            );
        }

        // 2. Dynamic Recent Payments Activity Entries
        if (activeLoan != null) {

            List<LoanPayments> payments =
                    loanPaymentsRepository
                            .findTop5ByLoanAccountIdOrderByPaymentDateDesc(
                                    activeLoan.getLoanAccountId());

            for (LoanPayments p : payments) {

                if (p.getPaymentDate() != null) {
                    list.add(
                            ActivityTimelineDto.builder()
                                    .activityDate(
                                            p.getPaymentDate()
                                                    .format(DATE_FORMATTER))
                                    .activityTime(
                                            p.getPaymentDate()
                                                    .format(TIME_FORMATTER))
                                    .title(
                                            p.getPaymentName() != null
                                                    ? p.getPaymentName()
                                                    : "EMI Payment Received")
                                    .description(
                                            "Payment of ₹ "
                                                    + p.getPaymentAmount()
                                                    + " processed successfully.")
                                    .build()
                    );
                }
            }
        }

        // 3. Dynamic Application Submission Activity Entry
        if (!deals.isEmpty()) {

            LoanDeal deal = deals.get(deals.size() - 1);

            if (deal.getCreatedAt() != null) {
                list.add(
                        ActivityTimelineDto.builder()
                                .activityDate(
                                        deal.getCreatedAt()
                                                .format(DATE_FORMATTER))
                                .activityTime(
                                        deal.getCreatedAt()
                                                .format(TIME_FORMATTER))
                                .title("Application Submitted")
                                .description(
                                        "Loan Application APP"
                                                + String.format("%05d", deal.getDealId())
                                                + " received.")
                                .build()
                );
            }
        }

        return list;
    }

    private String formatDynamicDocType(String docType) {

        if (docType == null || docType.trim().isEmpty()) {
            return "Document";
        }

        String cleaned = docType.replace("_", " ").trim();

        return cleaned.substring(0, 1).toUpperCase()
                + cleaned.substring(1).toLowerCase();
    }
}