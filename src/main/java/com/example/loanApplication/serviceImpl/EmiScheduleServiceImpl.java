package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.dto.PenaltyChargesRequestDto;
import com.example.loanApplication.entity.EmiSchedules;
import com.example.loanApplication.entity.LoanAccount;
//import com.example.loanApplication.service.EmailService;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.EmiSchedulesRepo;
import com.example.loanApplication.repository.LoanAccountRepository;
import com.example.loanApplication.service.EmiScheduleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public  class EmiScheduleServiceImpl  implements EmiScheduleService {

    private final ModelMapper modelMapper;
    private final EmiSchedulesRepo emiSchedulesRepo;
private  final LoanAccountRepository loanAccountRepo;
    @Override
    public List<EmiScheduleResponseDto> generateEmiScheduleByLoanAccountId(Integer loanAccountId) {



     List<EmiSchedules> emiSchedulesListofUser =   emiSchedulesRepo.findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(loanAccountId);
     return  emiSchedulesListofUser.stream().map(schedules->modelMapper.map(schedules,EmiScheduleResponseDto.class)).toList();
    }

    @Override
    public List<PenaltyChargesRequestDto> getItemizedEmiBreakdown(Long emiScheduleId) {
        return List.of();
    }

    //extra
//    @Override
//    public List<EmiScheduleResponseDto> getUserEmiSchedules(Integer loanAccountId) {
//        return List.of();
//    }

//    @Override
//    public List<EmiScheduleResponseDto> getUpcomingEmi(Integer loanAccountId) {
//        return List.of();
//    }

//    @Override
//    public List<EmiScheduleResponseDto> generateEmiSchedule(LoanAccount account) {
//        int emiday = account.getEmiDay();
//        BigDecimal getInterestRate = account.getInterestRate();
//        Integer tenuremonths = account.getTenureMonths();
//        BigDecimal p = account.getLoanAmount();
//        BigDecimal mr = getInterestRate.divide(BigDecimal.valueOf(1200, 10));
//        BigDecimal upperfactor = BigDecimal.ONE.add(mr);
//        BigDecimal power = BigDecimal.ONE;
//        for (int i = 0; i < tenuremonths; i++) {
//            power = power.multiply(upperfactor);
//            BigDecimal numerator = p.multiply(mr).multiply(power);
//
//            BigDecimal denominator = power.subtract(BigDecimal.ONE);
//
//            BigDecimal Emi = numerator.divide(denominator, 10, RoundingMode.HALF_UP);
//            Emi = Emi.setScale(2, RoundingMode.HALF_UP);
//            if (mr.compareTo(BigDecimal.ZERO) == 0) {
//                Emi = p.divide(
//                        BigDecimal.valueOf(tenuremonths),
//                        2,
//                        RoundingMode.HALF_UP
//                );
//            }
//            BigDecimal openingBalance = p;
//            LocalDate dueDate = account.getDisbursementDate().plusMonths(1).withDayOfMonth(emiday);
//            List<EmiSchedules> schedules = new ArrayList<>();
//            for (int installmentNo = 1; installmentNo <= tenuremonths; installmentNo++) {
//                BigDecimal interestAmount = openingBalance.multiply(mr).setScale(2, RoundingMode.HALF_UP);
//
//                BigDecimal principalAmount = Emi.subtract(interestAmount).setScale(2, RoundingMode.HALF_UP);
//                BigDecimal closingBalance = openingBalance.subtract(principalAmount).setScale(2, RoundingMode.HALF_UP);
//                if (installmentNo == tenuremonths) {
//                    principalAmount = openingBalance;
//                    Emi = principalAmount.add(interestAmount).setScale(2, RoundingMode.HALF_UP);
//                    closingBalance = BigDecimal.ZERO.setScale(2);
//
//                }
//
//
//                EmiSchedules schedule = new EmiSchedules();
//
//                schedule.setLoanAccount(
//                        account
//                );
//
//                schedule.setInstallmentNo(installmentNo);
//                schedule.setDueDate(dueDate);
//                schedule.setOpeningBalance(openingBalance);
//                schedule.setInterestAmount(interestAmount);
//                schedule.setPrincipalAmount(principalAmount);
//                schedule.setEmi(Emi);
//                schedule.setClosingBalance(closingBalance);
//                schedule.setPaymentStatus("PENDING");
//
//                schedules.add(schedule);
//
//                openingBalance = closingBalance;
//                dueDate = dueDate.plusMonths(1);
//
//
//            }
//            List<EmiSchedules> saveSchedules = emiSchedulesRepo.saveAll(schedules);
//
//            return saveSchedules.stream()
//                    .map(schedule ->
//                            modelMapper.map(
//                                    schedule,
//                                    EmiScheduleResponseDto.class
//                            )
//                    )
//                    .toList();
//
//
//        }
//
//    }





    @Override
    public List<EmiScheduleResponseDto> generateEmiSchedule(LoanAccount account) {

        int emiday = account.getEmiDay();
        BigDecimal getInterestRate = account.getInterestRate();
        Integer tenuremonths = account.getTenureMonths();
        BigDecimal p = account.getLoanAmount();

        BigDecimal mr = getInterestRate.divide(
                BigDecimal.valueOf(1200),
                10,
                RoundingMode.HALF_UP
        );

        BigDecimal Emi;

        // EMI calculation
        if (mr.compareTo(BigDecimal.ZERO) == 0) {

            Emi = p.divide(
                    BigDecimal.valueOf(tenuremonths),
                    2,
                    RoundingMode.HALF_UP
            );

        } else {

            BigDecimal upperfactor = BigDecimal.ONE.add(mr);

            BigDecimal power = upperfactor.pow(tenuremonths);

            BigDecimal numerator = p
                    .multiply(mr)
                    .multiply(power);

            BigDecimal denominator = power
                    .subtract(BigDecimal.ONE);

            Emi = numerator.divide(
                    denominator,
                    10,
                    RoundingMode.HALF_UP
            );

            Emi = Emi.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        BigDecimal openingBalance = p;

        LocalDate dueDate = account.getDisbursementDate()
                .plusMonths(1)
                .withDayOfMonth(emiday);

        List<EmiSchedules> schedules = new ArrayList<>();

        // Generate EMI schedules
        for (int installmentNo = 1;
             installmentNo <= tenuremonths;
             installmentNo++) {

            BigDecimal interestAmount = openingBalance
                    .multiply(mr)
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );

            BigDecimal principalAmount = Emi
                    .subtract(interestAmount)
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );

            BigDecimal closingBalance = openingBalance
                    .subtract(principalAmount)
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );

            // Last EMI adjustment
            if (installmentNo == tenuremonths) {

                principalAmount = openingBalance;

                Emi = principalAmount
                        .add(interestAmount)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

                closingBalance = BigDecimal.ZERO
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );
            }

            EmiSchedules schedule = new EmiSchedules();

            schedule.setLoanAccount(account);
            schedule.setInstallmentNo(installmentNo);
            schedule.setDueDate(dueDate);
            schedule.setOpeningBalance(openingBalance);
            schedule.setInterestAmount(interestAmount);
            schedule.setPrincipalAmount(principalAmount);
            schedule.setEmi(Emi);
            schedule.setClosingBalance(closingBalance);
            schedule.setPaymentStatus("PENDING");

            schedules.add(schedule);

            openingBalance = closingBalance;

            dueDate = dueDate.plusMonths(1);
        }

        List<EmiSchedules> saveSchedules =
                emiSchedulesRepo.saveAll(schedules);

        return saveSchedules.stream()
                .map(schedule ->
                        modelMapper.map(
                                schedule,
                                EmiScheduleResponseDto.class
                        )
                )
                .toList();
    }

        @Override
        public List<EmiScheduleResponseDto> regenerateEmiSchedule (Integer loanAccountId, BigDecimal newPrincipal){
            LoanAccount account = loanAccountRepo.findById(loanAccountId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Loan account not found with id: " + loanAccountId
                            )
                    );

            if (newPrincipal == null ||
                    newPrincipal.compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "New principal cannot be negative"
                );
            }

            List<EmiSchedules> pendingSchedules =
                    emiSchedulesRepo
                            .findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
                                    loanAccountId,
                                    "PENDING"
                            );

            if (pendingSchedules.isEmpty()) {
                throw new IllegalArgumentException(
                        "No pending EMI schedules found"
                );
            }

            BigDecimal annualRate = account.getInterestRate();

            BigDecimal monthlyRate = annualRate.divide(
                    BigDecimal.valueOf(1200),
                    10,
                    RoundingMode.HALF_UP
            );

            int remainingTenure = pendingSchedules.size();

            BigDecimal newEmi;

            if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {

                newEmi = newPrincipal.divide(
                        BigDecimal.valueOf(remainingTenure),
                        2,
                        RoundingMode.HALF_UP
                );

            } else {

                BigDecimal factor = BigDecimal.ONE.add(monthlyRate);

                BigDecimal power = factor.pow(remainingTenure);

                BigDecimal numerator = newPrincipal
                        .multiply(monthlyRate)
                        .multiply(power);

                BigDecimal denominator = power
                        .subtract(BigDecimal.ONE);

                newEmi = numerator.divide(
                        denominator,
                        10,
                        RoundingMode.HALF_UP
                ).setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal openingBalance = newPrincipal;

            for (int i = 0; i < pendingSchedules.size(); i++) {

                EmiSchedules schedule = pendingSchedules.get(i);

                BigDecimal interestAmount = openingBalance
                        .multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal principalAmount = newEmi
                        .subtract(interestAmount)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal closingBalance = openingBalance
                        .subtract(principalAmount)
                        .setScale(2, RoundingMode.HALF_UP);

                if (i == pendingSchedules.size() - 1) {

                    principalAmount = openingBalance;

                    newEmi = principalAmount
                            .add(interestAmount)
                            .setScale(2, RoundingMode.HALF_UP);

                    closingBalance = BigDecimal.ZERO
                            .setScale(2, RoundingMode.HALF_UP);
                }

                schedule.setOpeningBalance(openingBalance);
                schedule.setInterestAmount(interestAmount);
                schedule.setPrincipalAmount(principalAmount);
                schedule.setClosingBalance(closingBalance);
                schedule.setEmi(newEmi);

                openingBalance = closingBalance;
            }

            List<EmiSchedules> updatedSchedules =
                    emiSchedulesRepo.saveAll(pendingSchedules);

            return updatedSchedules.stream()
                    .map(schedule ->
                            modelMapper.map(
                                    schedule,
                                    EmiScheduleResponseDto.class
                            )
                    )
                    .toList();
        }
    }
