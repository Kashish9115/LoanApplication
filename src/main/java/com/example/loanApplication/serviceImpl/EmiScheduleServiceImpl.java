package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.entity.EmiSchedules;
import com.example.loanApplication.repository.EmiSchedulesRepo;
import com.example.loanApplication.service.EmiScheduleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EmiScheduleServiceImpl  implements EmiScheduleService {


    private  final EmiSchedulesRepo emiSchedulesRepo;
    private final ModelMapper modelMapper;

    @Override
    public List<EmiScheduleResponseDto> generateEmi(Integer loanAccountId) {
        LoanAccount loanAccount = loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new RuntimeException("Loan account not found"));

        if (!emiScheduleRepository
                .findByLoanAccount_LoanAccountId(loanAccountId)
                .isEmpty()) {
            throw new RuntimeException("EMI schedule already generated");
        }

        BigDecimal loanAmount = loanAccount.getLoanAmount();
        BigDecimal annualInterestRate = loanAccount.getInterestRate();
        Integer tenureMonths = loanAccount.getTenureMonths();

        if (loanAmount == null ||
                annualInterestRate == null ||
                tenureMonths == null) {
            throw new RuntimeException("Loan details are incomplete");
        }

        BigDecimal monthlyRate = annualInterestRate
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

      //  BigDecimal monthlyEmi = loanAccount.getEmiAmount();

        if (monthlyEmi == null) {
            if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {

                monthlyEmi = loanAmount.divide(
                        BigDecimal.valueOf(tenureMonths),
                        2,
                        RoundingMode.HALF_UP
                );

            } else {

                double power = Math.pow(
                        BigDecimal.ONE.add(monthlyRate).doubleValue(),
                        tenureMonths
                );

                BigDecimal numerator = loanAmount
                        .multiply(monthlyRate)
                        .multiply(BigDecimal.valueOf(power));

                BigDecimal denominator = BigDecimal.valueOf(power - 1);

                monthlyEmi = numerator.divide(
                        denominator,
                        2,
                        RoundingMode.HALF_UP
                );
            }
        }

     //   LocalDate firstDueDate = loanAccount.getDisbursementDate()
              //  .toLocalDate()
                //.plusMonths(1);

        BigDecimal openingBalance = loanAmount;

        List<EmiSchedule> schedules = new ArrayList<>();

        for (int installment = 1;
             installment <= tenureMonths;
             installment++) {

            BigDecimal interestAmount = openingBalance
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalAmount = monthlyEmi
                    .subtract(interestAmount);

            BigDecimal currentEmi = monthlyEmi;

            if (installment == tenureMonths) {

                principalAmount = openingBalance;

                currentEmi = principalAmount
                        .add(interestAmount)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal closingBalance = openingBalance
                    .subtract(principalAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            EmiSchedules schedule = new EmiSchedule();

            //schedule.setLoanAccount(loanAccount);
            schedule.setInstallmentNo(installment);
            schedule.setDueDate(
                    firstDueDate.plusMonths(installment - 1)
            );
            schedule.setPrincipalAmount(principalAmount);
            schedule.setInterestAmount(interestAmount);
            schedule.setOpeningBalance(openingBalance);
            schedule.setClosingBalance(closingBalance);
            schedule.setEmi(currentEmi);
            schedule.setPaymentStatus("PENDING");

            schedules.add(schedule);

            openingBalance = closingBalance;
        }

        List<EmiSchedules> savedSchedules =
                emiScheduleRepository.saveAll(schedules);

        return savedSchedules.stream()
                .map(schedule ->
                        modelMapper.map(
                                schedule,
                                EmiScheduleResponseDto.class
                        )
                )
                .toList();
    }





        return List.of();
    }

    @Override
    public List<EmiScheduleResponseDto> getUserEmiSchedules(Integer loanAccountId) {

        List<EmiSchedule> schedules =
                emiScheduleRepository
                        .findByLoanAccount_LoanAccountId(loanAccountId);

        return schedules.stream()
                .map(schedule ->
                        modelMapper.map(
                                schedule,
                                EmiScheduleResponseDto.class
                        )
                )
                .toList();


    //return List.of();
    }

    @Override
    public List<EmiScheduleResponseDto> getUpcomingEmi(Integer loanAccountId) {

        List<EmiSchedules> schedules =
                emiScheduleRepository
                        .findByLoanAccount_LoanAccountIdAndPaymentStatusNotAndDueDateGreaterThanEqualOrderByDueDateAsc(
                                loanAccountId,
                                "PAID",
                                LocalDate.now()
                        );

        return schedules.stream()
                .map(schedule ->
                        modelMapper.map(
                                schedule,
                                EmiScheduleResponseDto.class
                        )
                )
                .toList();


        return List.of();
    }

    @Override
    @Scheduled(cron = "0 0 2 * * *")

    public void fillEmiOnDue() {


        List<EmiSchedules> dueEmis =
                emiScheduleRepo
                        .findByDueDateLessThanEqualAndPaymentStatus(
                                LocalDate.now(),
                                "PENDING"
                        );

        for (EmiSchedules emi : dueEmis) {

            // Payment processing
            // Penalty processing if required
            // Notification
            // Email
        }
    }
}