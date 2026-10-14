package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.*;
import com.example.loanApplication.entity.EmiSchedules;
import com.example.loanApplication.entity.LoanAccount;
//import com.example.loanApplication.service.EmailService;
import com.example.loanApplication.entity.PenaltyCharges;
import com.example.loanApplication.enumeration.PaymentStatus;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.EmiSchedulesRepo;
import com.example.loanApplication.repository.LoanAccountRepository;
import com.example.loanApplication.repository.PenaltyChargesRepo;
import com.example.loanApplication.service.EmiScheduleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public  class EmiScheduleServiceImpl  implements EmiScheduleService {

    private final ModelMapper modelMapper;
    private final EmiSchedulesRepo emiSchedulesRepo;
private  final LoanAccountRepository loanAccountRepo;
private  final PenaltyChargesRepo penaltyChargesRepo;


@Override
@Transactional
public List<EmiScheduleResponseDto> generateInitialSchedule(
        Integer loanAccountId) {

    LoanAccount account = loanAccountRepo.findById(loanAccountId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Loan account not found: " + loanAccountId));

    BigDecimal principal = account.getLoanAmount();
    BigDecimal annualRate = account.getInterestRate();
    BigDecimal emi = account.getEmiAmount();

    int tenure = account.getTenureMonths();
    int emiDay = account.getEmiDay();

    if (emiSchedulesRepo
            .findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(
                    loanAccountId).size() > 0) {
        throw new IllegalStateException(
                "EMI schedule already exists for this loan account");
    }

    if (principal == null || annualRate == null || emi == null
            || principal.signum() <= 0 || annualRate.signum() < 0
            || emi.signum() <= 0 || tenure <= 0
            || emiDay < 1 || emiDay > 31
            || account.getDisbursementDate() == null) {
        throw new IllegalArgumentException(
                "Invalid loan details for EMI generation");
    }

    BigDecimal monthlyRate = annualRate.divide(
            BigDecimal.valueOf(1200),
            10,
            RoundingMode.HALF_UP);

    BigDecimal openingBalance = principal;

    LocalDate firstDueDate =
            account.getDisbursementDate().plusMonths(1);

    List<EmiSchedules> schedules = new ArrayList<>();

    for (int i = 1; i <= tenure; i++) {

        BigDecimal interest = openingBalance
                .multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal principalAmount = emi.subtract(interest)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal installmentEmi = emi;

        // Adjust the final installment for rounding
        if (i == tenure) {
            principalAmount = openingBalance;
            installmentEmi = principalAmount.add(interest)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal closingBalance = openingBalance
                .subtract(principalAmount)
                .setScale(2, RoundingMode.HALF_UP);

        LocalDate month = firstDueDate.plusMonths(i - 1);
        LocalDate dueDate = month.withDayOfMonth(
                Math.min(emiDay, month.lengthOfMonth()));

        EmiSchedules schedule = new EmiSchedules();

        schedule.setLoanAccount(account);
        schedule.setInstallmentNo(i);
        schedule.setDueDate(dueDate);
        schedule.setOpeningBalance(openingBalance);
        schedule.setInterestAmount(interest);
        schedule.setPrincipalAmount(principalAmount);
        schedule.setEmi(installmentEmi);
        schedule.setClosingBalance(closingBalance);
        schedule.setPaymentStatus(PaymentStatus.PENDING.name());

        schedules.add(schedule);
        openingBalance = closingBalance;
    }

    List<EmiSchedules> savedSchedules =
            emiSchedulesRepo.saveAll(schedules);

    return savedSchedules.stream()
            .map(s -> modelMapper.map(
                    s, EmiScheduleResponseDto.class))
            .toList();
}
















@Override
@Transactional
public List<EmiScheduleResponseDto> regenerateScheduleOnPartialForeclosure(
        Integer loanAccountId,
        BigDecimal finalSettlementAmount) {

    // 1. Fetch loan account
    LoanAccount account = loanAccountRepo.findById(loanAccountId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Loan Account not found: " + loanAccountId));

    // 2. Validate remaining principal
    if (finalSettlementAmount == null
            || finalSettlementAmount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException(
                "Remaining principal must be greater than zero");
    }

    // 3. Get existing EMI and interest rate
    BigDecimal emiAmount = account.getEmiAmount();
    BigDecimal annualRate = account.getInterestRate();

    if (emiAmount == null
            || emiAmount.compareTo(BigDecimal.ZERO) <= 0
            || annualRate == null
            || annualRate.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException(
                "Invalid EMI amount or interest rate");
    }

    // 4. Fetch pending schedules only
    List<EmiSchedules> pendingSchedules =
            emiSchedulesRepo
                    .findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
                            loanAccountId, "PENDING");

    if (pendingSchedules.isEmpty()) {
        throw new IllegalStateException(
                "No pending EMI schedules found for loan account: "
                        + loanAccountId);
    }

    // 5. Calculate monthly interest rate
    BigDecimal monthlyRate = annualRate.divide(
            new BigDecimal("1200"),
            10,
            RoundingMode.HALF_UP);

    BigDecimal openingBalance = finalSettlementAmount
            .setScale(2, RoundingMode.HALF_UP);

    List<EmiSchedules> updatedSchedules = new ArrayList<>();

    int nextIndex = 0;

    // 6. Recalculate every pending installment
    for (; nextIndex < pendingSchedules.size(); nextIndex++) {

        EmiSchedules schedule = pendingSchedules.get(nextIndex);

        // Calculate interest on remaining principal
        BigDecimal interestAmount = openingBalance
                .multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        // Remaining principal + interest
        BigDecimal totalDue = openingBalance.add(interestAmount);

        BigDecimal installmentEmi;
        BigDecimal principalAmount;
        BigDecimal closingBalance;

        // 7. Adjust final installment
        if (totalDue.compareTo(emiAmount) <= 0) {

            installmentEmi = totalDue;
            principalAmount = openingBalance;
            closingBalance = BigDecimal.ZERO.setScale(2);

        } else {

            // Keep regular EMI unchanged
            installmentEmi = emiAmount;

            // EMI = Principal + Interest
            principalAmount = installmentEmi.subtract(interestAmount);

            if (principalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                        "EMI must be greater than monthly interest");
            }

            closingBalance = openingBalance
                    .subtract(principalAmount)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // 8. Update existing pending schedule
        schedule.setOpeningBalance(openingBalance);
        schedule.setInterestAmount(interestAmount);
        schedule.setPrincipalAmount(principalAmount);
        schedule.setEmi(installmentEmi);
        schedule.setClosingBalance(closingBalance);

        updatedSchedules.add(schedule);

        openingBalance = closingBalance;

        // Stop when the loan is fully repaid
        if (closingBalance.compareTo(BigDecimal.ZERO) == 0) {
            nextIndex++;
            break;
        }
    }

    // 9. Ensure all remaining principal is covered
    if (openingBalance.compareTo(BigDecimal.ZERO) > 0) {
        throw new IllegalStateException(
                "Existing pending schedules are insufficient "
                        + "to repay the remaining principal");
    }

    // 10. Delete excess pending installments
    if (nextIndex < pendingSchedules.size()) {
        emiSchedulesRepo.deleteAll(
                pendingSchedules.subList(nextIndex, pendingSchedules.size()));
    }

    // 11. Update remaining outstanding principal
    account.setOutstandingAmount(
            finalSettlementAmount.setScale(2, RoundingMode.HALF_UP));

    loanAccountRepo.save(account);

    // 12. Save regenerated schedules
    List<EmiSchedules> savedSchedules =
            emiSchedulesRepo.saveAll(updatedSchedules);

    // 13. Convert entities to response DTOs
    return savedSchedules.stream()
            .map(schedule ->
                    modelMapper.map(schedule, EmiScheduleResponseDto.class))
            .collect(Collectors.toList());
}




    @Override
    @Transactional
    public EmiBreakDownDto getEmiBreakdown(Integer emiScheduleId) {

        // 1. Fetch EMI schedule
        EmiSchedules schedule = emiSchedulesRepo.findById(emiScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EMI Schedule not found for ID: " + emiScheduleId
                ));

        BigDecimal baseEmi = schedule.getEmi();
        BigDecimal bounceCharge = BigDecimal.ZERO;
        BigDecimal lateFee = BigDecimal.ZERO;

        LocalDate today = LocalDate.now();
        LocalDate dueDate = schedule.getDueDate();

        // 2. Apply charges only if EMI is pending and overdue
        if ("PENDING".equalsIgnoreCase(schedule.getPaymentStatus())
                && today.isAfter(dueDate)) {

            // 3. Fetch existing bounce penalty
            PenaltyCharges penalty = penaltyChargesRepo
                    .findByEmiSchedule_EmiScheduleId(emiScheduleId)
                    .orElse(null);

            if (penalty != null
                    && "APPLIED".equalsIgnoreCase(penalty.getStatus())) {

                bounceCharge = penalty.getPenaltyAmount() != null
                        ? penalty.getPenaltyAmount()
                        : BigDecimal.ZERO;

            } else if (penalty == null) {

                // Expected bounce charge before scheduler creates the record
                bounceCharge = new BigDecimal("600.00");
            }

            // 4. Calculate late fee: ₹1 per overdue day
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);

            lateFee = BigDecimal.valueOf(daysOverdue)
                    .multiply(BigDecimal.ONE);
        }

        // 5. Calculate total payable
        BigDecimal totalPayable = baseEmi
                .add(bounceCharge)
                .add(lateFee);

        // 6. Return EMI breakdown
        return new EmiBreakDownDto(
                schedule.getLoanAccount().getLoanAccountId(),
                schedule.getInstallmentNo(),
                schedule.getDueDate(),
                schedule.getPaymentStatus(),
                baseEmi,
                bounceCharge,
                lateFee,
                totalPayable,
                schedule.getEmiScheduleId()
        );
    }



    @Override
    @Transactional
    public OverdueEmiPaymentBreakdownDto getAllOverdueEmiBreakdown(
            Integer loanAccountId) {

        // 1. Verify loan account exists
        loanAccountRepo.findById(loanAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found: " + loanAccountId
                ));

        LocalDate today = LocalDate.now();

        // 2. Fetch all pending EMIs for this loan account
        List<EmiSchedules> pendingEmis =
                emiSchedulesRepo
                        .findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
                                loanAccountId, "PENDING"
                        );

        // 3. Keep only overdue EMIs
        List<EmiSchedules> overdueEmis = pendingEmis.stream()
                .filter(emi -> emi.getDueDate() != null)
                .filter(emi -> emi.getDueDate().isBefore(today))
                .toList();

        // 4. Calculate totals
        BigDecimal totalBaseEmi = BigDecimal.ZERO;
        BigDecimal totalBounceCharge = BigDecimal.ZERO;
        BigDecimal totalLateFee = BigDecimal.ZERO;
        BigDecimal totalPayable = BigDecimal.ZERO;

        List<EmiBreakDownDto> breakdownList = new ArrayList<>();

        for (EmiSchedules emi : overdueEmis) {

            EmiBreakDownDto breakdown =
                    getEmiBreakdown(emi.getEmiScheduleId());

            breakdownList.add(breakdown);

            totalBaseEmi = totalBaseEmi.add(
                    breakdown.getBaseEmi()
            );

            totalBounceCharge = totalBounceCharge.add(
                    breakdown.getBounceCharge()
            );

            totalLateFee = totalLateFee.add(
                    breakdown.getLateFeePerDay()
            );

            totalPayable = totalPayable.add(
                    breakdown.getTotalPayableAmount()
            );
        }

        // 5. Return consolidated amount
        return OverdueEmiPaymentBreakdownDto.builder()
                .loanAccountId(loanAccountId)
                .overdueEmis(breakdownList)
                .totalOverdueEmis(breakdownList.size())
                .totalBaseEmi(totalBaseEmi)
                .totalBounceCharge(totalBounceCharge)
                .totalLateFee(totalLateFee)
                .totalPayableAmount(totalPayable)
                .build();
    }
}
