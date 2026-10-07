package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.entity.EmiSchedules;
import com.example.loanApplication.exception.BusinessException;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.EmiSchedulesRepo;
import com.example.loanApplication.service.EmiScheduleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EmiScheduleServiceImpl  implements EmiScheduleService {


    private final EmiSchedulesRepo emiSchedulesRepo;
    private final ModelMapper modelMapper;

    @Override
    public List<EmiScheduleResponseDto> generateEmi(Integer loanAccountId) {
        LoanAccount loanAccount = loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan account not found: " + loanAccountId));

        if (emiSchedulesRepo.existsByLoanAccount_LoanAccountId(loanAccountId)) {
            throw new BusinessException(
                    "EMI schedule already exists for loan account: " + loanAccountId);
        }

        BigDecimal loanAmount = loanAccount.getLoanAmount();
        BigDecimal annualInterestRate = loanAccount.getInterestRate();
        Integer tenureMonths = loanAccount.getTenureMonths();

        if (loanAmount == null ||
                annualInterestRate == null ||
                tenureMonths == null ||
                tenureMonths <= 0) {

            throw new BusinessException(
                    "Incomplete loan details for EMI calculation");
        }

        BigDecimal monthlyInterestRate = annualInterestRate
                .divide(
                        BigDecimal.valueOf(1200),
                        10,
                        RoundingMode.HALF_UP);

        BigDecimal monthlyEmi = loanAccount.getEmiAmount();

        if (monthlyEmi == null) {

            if (monthlyInterestRate.compareTo(BigDecimal.ZERO) == 0) {

                monthlyEmi = loanAmount.divide(
                        BigDecimal.valueOf(tenureMonths),
                        2,
                        RoundingMode.HALF_UP);

            } else {

                BigDecimal factor = BigDecimal.ONE
                        .add(monthlyInterestRate)
                        .pow(tenureMonths);

                monthlyEmi = loanAmount
                        .multiply(monthlyInterestRate)
                        .multiply(factor)
                        .divide(
                                factor.subtract(BigDecimal.ONE),
                                2,
                                RoundingMode.HALF_UP);
            }
        }

        LocalDate dueDate = loanAccount.getDisbursementDate()
                .toLocalDate()
                .plusMonths(1);

        BigDecimal openingBalance = loanAmount;

        List<EmiSchedules> schedules = new ArrayList<>();

        for (int installmentNo = 1;
             installmentNo <= tenureMonths;
             installmentNo++) {

            BigDecimal interestAmount = openingBalance
                    .multiply(monthlyInterestRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalAmount =
                    monthlyEmi.subtract(interestAmount);

            BigDecimal emi = monthlyEmi;

            if (installmentNo == tenureMonths) {

                principalAmount = openingBalance;
                emi = principalAmount.add(interestAmount);
            }

            BigDecimal closingBalance = openingBalance
                    .subtract(principalAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            EmiSchedules schedule = new EmiSchedules();

            schedule.setLoanAccount(loanAccount);
            schedule.setInstallmentNo(installmentNo);
            schedule.setDueDate(dueDate);
            schedule.setPrincipalAmount(principalAmount);
            schedule.setInterestAmount(interestAmount);
            schedule.setOpeningBalance(openingBalance);
            schedule.setClosingBalance(closingBalance);
            schedule.setEmi(emi);
            schedule.setPaymentStatus("PENDING");

            schedules.add(schedule);

            openingBalance = closingBalance;
            dueDate = dueDate.plusMonths(1);
        }

        List<EmiSchedules> savedSchedules =
                emiSchedulesRepo.saveAll(schedules);

        return savedSchedules.stream()
                .map(schedule -> {
                    EmiScheduleResponseDto dto =
                            modelMapper.map(
                                    schedule,
                                    EmiScheduleResponseDto.class);

                    dto.setLoanAccountId(
                            schedule.getLoanAccount()
                                    .getLoanAccountId());

                    return dto;
                })
                .toList();

    }

    @Override
    public List<EmiScheduleResponseDto> getUserEmiSchedules(Integer loanAccountId) {

        if (!loanAccountRepository.existsById(loanAccountId)) {
            throw new ResourceNotFoundException(
                    "Loan account not found: " + loanAccountId);
        }

        List<EmiSchedules> schedules =
                emiSchedulesRepo
                        .findByLoanAccount_LoanAccountId(
                                loanAccountId);

        return schedules.stream()
                .map(schedule -> {
                    EmiScheduleResponseDto dto =
                            modelMapper.map(
                                    schedule,
                                    EmiScheduleResponseDto.class);

                    dto.setLoanAccountId(
                            schedule.getLoanAccount()
                                    .getLoanAccountId());

                    return dto;
                })
                .toList();


        //return List.of();
    }

    @Override
    public List<EmiScheduleResponseDto> getUpcomingEmi(Integer loanAccountId) {

        if (!loanAccountRepository.existsById(loanAccountId)) {
            throw new ResourceNotFoundException(
                    "Loan account not found: " + loanAccountId);
        }

        List<EmiSchedules> schedules =
                emiSchedulesRepo
                        .findByLoanAccount_LoanAccountIdAndPaymentStatusNotAndDueDateGreaterThanEqualOrderByDueDateAsc(
                                loanAccountId,
                                "PAID",
                                LocalDate.now());

        return schedules.stream()
                .map(schedule -> {
                    EmiScheduleResponseDto dto =
                            modelMapper.map(
                                    schedule,
                                    EmiScheduleResponseDto.class);

                    dto.setLoanAccountId(
                            schedule.getLoanAccount()
                                    .getLoanAccountId());

                    return dto;
                })
                .toList();


        //  return List.of();
    }

    @Scheduled(cron ="0 0 2 * * *" )
    @Override
    public void fillEmiOnDue() {
        List<EmiSchedules> dueEmis =
                emiSchedulesRepo
                        .findByDueDateLessThanEqualAndPaymentStatus(
                                LocalDate.now(),
                                "PENDING");

        for (EmiSchedules emi : dueEmis) {

            // Payment processing
            // Penalty processing
            // Notification
            // Email notification
        }

    }

    @Override
    public void cancelFutureEmis(Integer loanAccountId, String reason) {
        List<EmiSchedules> schedules =
                emiSchedulesRepo
                        .findByLoanAccount_LoanAccountIdAndPaymentStatus(
                                loanAccountId,
                                "PENDING");

        LocalDate today = LocalDate.now();

        for (EmiSchedules schedule : schedules) {

            if (schedule.getDueDate().isAfter(today)) {

                schedule.setPaymentStatus("CANCELLED");
                schedule.setCancellationReason(reason);


            }
        }

        @Override
        public List<EmiScheduleResponseDto> recalculateEmiSchedule (Integer loanAccountId){

// Partial foreclosure logic will come here.
            // Future EMI schedule will be cancelled
            // and new schedule will be generated
            // based on updated outstanding principal.

            return List.of();
        }

    }
}
