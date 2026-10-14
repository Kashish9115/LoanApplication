package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.*;
import com.example.loanApplication.entity.EmiSchedules;
import com.example.loanApplication.entity.LoanAccount;
//import com.example.loanApplication.service.EmailService;
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
//    @Override
//    public List<EmiScheduleResponseDto> generateInitialSchedule(Integer loanAccountId) {
//      //  log.info("Generating initial EMI schedule for LoanAccountId: {}", loanAccountId);
//
//        LoanAccount account = loanAccountRepo.findById(loanAccountId)
//                .orElseThrow(() -> new ResourceNotFoundException("Loan Account not found ID: " + loanAccountId));
//
//        BigDecimal emiAmount = account.getEmiAmount();
//        Integer emiDay = account.getEmiDay() ;//;!= null ? account.getEmiDay() : 5;
//        Integer tenureMonths = account.getTenureMonths();
//        BigDecimal openingBal = account.getLoanAmount();
//
//        List<EmiSchedules> schedules = new ArrayList<>();
//        LocalDate startDate = LocalDate.now().plusMonths(1).withDayOfMonth(Math.min(emiDay, 28));
//
//        for (int i = 1; i <= tenureMonths; i++) {
//            BigDecimal closingBal = openingBal.subtract(emiAmount);
//            if (closingBal.compareTo(BigDecimal.ZERO) < 0) closingBal = BigDecimal.ZERO;
//
//            EmiSchedules schedule = new EmiSchedules();
//            schedule.setLoanAccount(account);
//            schedule.setInstallmentNo(i);
//            schedule.setDueDate(startDate.plusMonths(i - 1));
//            schedule.setOpeningBalance(openingBal);
//            schedule.setEmi(emiAmount);
//            schedule.setClosingBalance(closingBal);
//            schedule.setPaymentStatus(PaymentStatus.PENDING);
//
//            schedules.add(schedule);
//            openingBal = closingBal;
//        }
//
//        List<EmiSchedules> saved = emiSchedulesRepo.saveAll(schedules);
//    //    log.info("Saved {} EMI schedule records for LoanAccount {}", saved.size(), loanAccountId);
//
//        return saved.stream()
//                .map(s -> modelMapper.map(s, EmiScheduleResponseDto.class))
//                .collect(Collectors.toList());
//
//    }








    @Override
    @Transactional
    public LoanPaymentResponseDTO processPaymentLedger(LoanPaymentRequestDTO dto) {
        log.info("Processing EMI payment for LoanAccountId: {}, ScheduleId: {}, Amount: ₹{}",
                dto.getLoanAccountId(), dto.getEmiScheduleId(), dto.getPaymentAmount());

        // 1. Fetch Loan Account
        LoanAccount account = loanAccountRepo.findById(dto.getLoanAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan Account not found for ID: " + dto.getLoanAccountId()));

        // 2. Validate EMI Schedule and check required payment amount
        if (dto.getEmiScheduleId() != null) {
            // Get itemized breakdown (Base EMI + Bounce Charges + Daily Late Fee)
            EmiBreakDownDto breakdown = emiService.getEmiBreakdown(dto.getEmiScheduleId());
            BigDecimal totalRequiredAmount = breakdown.getTotalPayableAmount();

            // Check if already paid
            if ("PAID".equalsIgnoreCase(breakdown.getPaymentStatus())) {
                throw new IllegalStateException("This EMI installment has already been PAID.");
            }

            // 👈 STRICT VALIDATION: User cannot pay less than the exact total required amount
            if (dto.getPaymentAmount().compareTo(totalRequiredAmount) < 0) {
                log.error("Payment rejected! User attempted to pay ₹{}, but exact required amount is ₹{}",
                        dto.getPaymentAmount(), totalRequiredAmount);
                throw new InvalidPaymentAmountException(
                        "Payment amount (₹" + dto.getPaymentAmount() +
                                ") cannot be less than the required payable amount (₹" + totalRequiredAmount + ")"
                );
            }

            // Mark target EMI Schedule as PAID
            EmiSchedules schedule = emiSchedulesRepo.findById(dto.getEmiScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("EMI Schedule not found for ID: " + dto.getEmiScheduleId()));

            schedule.setPaymentStatus("PAID");
            schedule.setPaidDate(LocalDateTime.now());
            emiSchedulesRepo.save(schedule);
        }

        // 3. Create and Save entry in LoanPayments entity
        LoanPayments payment = new LoanPayments();
        payment.setLoanAccountId(account.getLoanAccountId());
        payment.setPaymentAmount(dto.getPaymentAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentName(dto.getPaymentName() != null ? dto.getPaymentName() : "EMI Repayment");

        LoanPayments savedPayment = loanPaymentRepository.save(payment);
        log.info("Saved transaction entry in LoanPayments table with Payment ID: {}", savedPayment.getPaymentId());

        // 4. Update Outstanding Balance on LoanAccount
        BigDecimal currentOutstanding = account.getOutstandingAmount() != null ? account.getOutstandingAmount() : BigDecimal.ZERO;
        BigDecimal newOutstanding = currentOutstanding.subtract(dto.getPaymentAmount());

        if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            newOutstanding = BigDecimal.ZERO;
        }

        account.setOutstandingAmount(newOutstanding);
        loanAccountRepository.save(account);
        log.info("Updated OutstandingAmount for LoanAccountId {}: ₹{}", account.getLoanAccountId(), newOutstanding);

        // 5. Send Notification Confirmation to Borrower
        if (account.getCustomerId() != null) {
            notificationService.sendNotification(
                    account.getCustomerId(),
                    "Payment of ₹" + dto.getPaymentAmount() + " processed successfully for Loan Account No: " + account.getLoanAccountNo() + ". Ref ID: " + savedPayment.getPaymentId(),
                    "EMI Payment Confirmation Receipt"
            );
        }

        // 6. Map response DTO using ModelMapper
        LoanPaymentResponseDTO response = modelMapper.map(savedPayment, LoanPaymentResponseDTO.class);
        response.setOutstandingAmount(account.getOutstandingAmount());
        response.setMessage("Payment verified and database ledgers updated successfully.");
        return response;
    }



    @Override
    public EmiBreakDownDto getEmiBreakdown(Integer emiScheduleId) {
        EmiSchedules schedule = emiSchedulesRepo.findById(emiScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("EMI Schedule not found for ID: " + emiScheduleId));

        BigDecimal baseEmi = schedule.getEmi();
        BigDecimal bounceCharge = BigDecimal.ZERO;
        BigDecimal lateFee = BigDecimal.ZERO;

        LocalDate today = LocalDate.now();
        LocalDate dueDate = schedule.getDueDate();

        // Check if payment is overdue and pending
        if ("PENDING".equalsIgnoreCase(schedule.getPaymentStatus()) && today.isAfter(dueDate)) {
            // Apply ₹600 Bounce Charge
            bounceCharge = new BigDecimal("600.00");

            // Apply ₹1 per day for every day overdue
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);
            lateFee = new BigDecimal(daysOverdue); // ₹1 * daysOverdue
        }

        BigDecimal totalPayable = baseEmi.add(bounceCharge).add(lateFee);

        return new EmiBreakDownDto(
                schedule.getEmiScheduleId(),
                schedule.getLoanAccount().getLoanAccountId(),
                schedule.getInstallmentNo(),
                schedule.getDueDate(),
                baseEmi,
                bounceCharge,
                lateFee,
                totalPayable,
                schedule.getPaymentStatus()
        );
    }

    // will be given by savita
    @Override
    @Transactional
    public List<EmiScheduleResponseDto> regenerateScheduleOnPartialForeclosure(
            Integer loanAccountId,
            BigDecimal finalSettlementAmount) {

        log.info(
                "Regenerating EMI schedule for LoanAccountId: {}, FinalSettlementAmount: {}",
                loanAccountId,
                finalSettlementAmount
        );

        // 1. Fetch loan account
        LoanAccount account = loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan Account not found: " + loanAccountId));

        // 2. Validate final settlement amount (Remaining Balance)
        if (finalSettlementAmount == null
                || finalSettlementAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Final settlement amount must be greater than zero");
        }

        // 3. Validate existing EMI amount
        BigDecimal emiAmount = account.getEmiAmount();

        if (emiAmount == null
                || emiAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "EMI amount must be greater than zero");
        }

        // 4. Fetch pending EMI schedules
        List<EmiSchedules> pendingSchedules = emiSchedulesRepo
                .findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
                        loanAccountId, "PENDING");

        if (pendingSchedules.isEmpty()) {
            throw new IllegalStateException(
                    "No pending EMI schedules found for loan account: "
                            + loanAccountId);
        }

        // 👈 FIX 1: LoanAccount ka Outstanding Amount database me update karein
        account.setOutstandingAmount(finalSettlementAmount);
        loanAccountRepository.save(account);
        log.info("Updated LoanAccount {} outstanding amount to ₹{}", loanAccountId, finalSettlementAmount);

        // 5. Calculate the required number of installments
        int requiredMonths = finalSettlementAmount
                .divide(emiAmount, 0, RoundingMode.CEILING)
                .intValueExact();

        // 6. Delete excess pending schedules if tenure reduces
        if (pendingSchedules.size() > requiredMonths) {
            List<EmiSchedules> excessSchedules =
                    new ArrayList<>(
                            pendingSchedules.subList(
                                    requiredMonths,
                                    pendingSchedules.size()));

            emiSchedulesRepo.deleteAll(excessSchedules);

            pendingSchedules = new ArrayList<>(
                    pendingSchedules.subList(0, requiredMonths));
        }

        // 7. Update the remaining EMI schedules
        BigDecimal openingBalance = finalSettlementAmount;

        for (EmiSchedules schedule : pendingSchedules) {

            BigDecimal closingBalance = openingBalance.subtract(emiAmount);

            if (closingBalance.compareTo(BigDecimal.ZERO) < 0) {
                closingBalance = BigDecimal.ZERO;
            }

            schedule.setOpeningBalance(openingBalance);
            schedule.setClosingBalance(closingBalance);

            openingBalance = closingBalance;
        }

        // 8. Save updated schedules
        List<EmiSchedules> updatedSchedules =
                emiSchedulesRepo.saveAll(pendingSchedules);

        log.info(
                "EMI schedule regenerated successfully for loanAccountId: {}",
                loanAccountId);

        // 9. Return response
        return updatedSchedules.stream()
                .map(schedule ->
                        modelMapper.map(schedule, EmiScheduleResponseDto.class))
                .collect(Collectors.toList());
    }

//    @Override
//    public List<EmiScheduleResponseDto> generateEmiScheduleByLoanAccountId(Integer loanAccountId) {
//
//
//
//     List<EmiSchedules> emiSchedulesListofUser =   emiSchedulesRepo.findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(loanAccountId);
//    // return  emiSchedulesListofUser.stream().map(schedules->modelMapper.map(schedules,EmiScheduleResponseDto.class)).toList();
//
//
//
//        return emiSchedulesListofUser.stream()
//                .map(this::mapToResponseDto)
//                .toList();
//
//    }

//    @Override
//    public List<PenaltyChargesRequestDto> getItemizedEmiBreakdown(Long emiScheduleId) {
//        return List.of();
//    }

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





//    @Override
//    public List<EmiScheduleResponseDto> generateEmiSchedule(LoanAccount account) {
//
//        int emiday = account.getEmiDay();
//        BigDecimal getInterestRate = account.getInterestRate();
//        Integer tenuremonths = account.getTenureMonths();
//        BigDecimal p = account.getLoanAmount();
//
//        BigDecimal mr = getInterestRate.divide(
//                BigDecimal.valueOf(1200),
//                10,
//                RoundingMode.HALF_UP
//        );
//
//        BigDecimal Emi;
//
//        // EMI calculation
//        if (mr.compareTo(BigDecimal.ZERO) == 0) {
//
//            Emi = p.divide(
//                    BigDecimal.valueOf(tenuremonths),
//                    2,
//                    RoundingMode.HALF_UP
//            );
//
//        } else {
//
//            BigDecimal upperfactor = BigDecimal.ONE.add(mr);
//
//            BigDecimal power = upperfactor.pow(tenuremonths);
//
//            BigDecimal numerator = p
//                    .multiply(mr)
//                    .multiply(power);
//
//            BigDecimal denominator = power
//                    .subtract(BigDecimal.ONE);
//
//            Emi = numerator.divide(
//                    denominator,
//                    10,
//                    RoundingMode.HALF_UP
//            );
//
//            Emi = Emi.setScale(
//                    2,
//                    RoundingMode.HALF_UP
//            );
//        }
//
//        BigDecimal openingBalance = p;
//
//        LocalDate dueDate = account.getDisbursementDate()
//                .plusMonths(1)
//                .withDayOfMonth(emiday);
//
//        List<EmiSchedules> schedules = new ArrayList<>();
//
//        // Generate EMI schedules
//        for (int installmentNo = 1;
//             installmentNo <= tenuremonths;
//             installmentNo++) {
//
//            BigDecimal interestAmount = openingBalance
//                    .multiply(mr)
//                    .setScale(
//                            2,
//                            RoundingMode.HALF_UP
//                    );
//
//            BigDecimal principalAmount = Emi
//                    .subtract(interestAmount)
//                    .setScale(
//                            2,
//                            RoundingMode.HALF_UP
//                    );
//
//            BigDecimal closingBalance = openingBalance
//                    .subtract(principalAmount)
//                    .setScale(
//                            2,
//                            RoundingMode.HALF_UP
//                    );
//
//            // Last EMI adjustment
//            if (installmentNo == tenuremonths) {
//
//                principalAmount = openingBalance;
//
//                Emi = principalAmount
//                        .add(interestAmount)
//                        .setScale(
//                                2,
//                                RoundingMode.HALF_UP
//                        );
//
//                closingBalance = BigDecimal.ZERO
//                        .setScale(
//                                2,
//                                RoundingMode.HALF_UP
//                        );
//            }
//
//            EmiSchedules schedule = new EmiSchedules();
//
//            schedule.setLoanAccount(account);
//            schedule.setInstallmentNo(installmentNo);
//            schedule.setDueDate(dueDate);
//            schedule.setOpeningBalance(openingBalance);
//            schedule.setInterestAmount(interestAmount);
//            schedule.setPrincipalAmount(principalAmount);
//            schedule.setEmi(Emi);
//            schedule.setClosingBalance(closingBalance);
//            schedule.setPaymentStatus("PENDING");
//
//            schedules.add(schedule);
//
//            openingBalance = closingBalance;
//
//            dueDate = dueDate.plusMonths(1);
//        }
//
//        List<EmiSchedules> saveSchedules =
//                emiSchedulesRepo.saveAll(schedules);
//
////        return saveSchedules.stream()
////                .map(schedule ->
////                        modelMapper.map(
////                                schedule,
////                                EmiScheduleResponseDto.class
////                        )
////                )
////                .toList();
//
//
//        return saveSchedules.stream()
//                .map(this::mapToResponseDto)
//                .toList();
//    }
//
//
//    private EmiScheduleResponseDto mapToResponseDto(EmiSchedules schedule) {
//
//        EmiScheduleResponseDto dto = new EmiScheduleResponseDto();
//
//        dto.setEmiScheduleId(schedule.getEmiScheduleId());
//        dto.setLoanAccountId(schedule.getLoanAccount().getLoanAccountId());
//        dto.setInstallmentNo(schedule.getInstallmentNo());
//        dto.setDueDate(schedule.getDueDate());
//        dto.setPrincipalAmount(schedule.getPrincipalAmount());
//        dto.setInterestAmount(schedule.getInterestAmount());
//        dto.setOpeningBalance(schedule.getOpeningBalance());
//        dto.setClosingBalance(schedule.getClosingBalance());
//        dto.setEmi(schedule.getEmi());
//        dto.setPaymentStatus(schedule.getPaymentStatus());
//        dto.setPaidDate(schedule.getPaidDate());
//        dto.setCancellationReason(schedule.getCancellationReason());
//
//        return dto;
//    }
//
//
//        @Override
//        public List<EmiScheduleResponseDto> regenerateEmiSchedule (Integer loanAccountId, BigDecimal newPrincipal){
//            LoanAccount account = loanAccountRepo.findById(loanAccountId)
//                    .orElseThrow(() ->
//                            new ResourceNotFoundException(
//                                    "Loan account not found with id: " + loanAccountId
//                            )
//                    );
//
//            if (newPrincipal == null ||
//                    newPrincipal.compareTo(BigDecimal.ZERO) < 0) {
//
//                throw new IllegalArgumentException(
//                        "New principal cannot be negative"
//                );
//            }
//
//            List<EmiSchedules> pendingSchedules =
//                    emiSchedulesRepo
//                            .findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
//                                    loanAccountId,
//                                    "PENDING"
//                            );
//
//            if (pendingSchedules.isEmpty()) {
//                throw new IllegalArgumentException(
//                        "No pending EMI schedules found"
//                );
//            }
//
//            BigDecimal annualRate = account.getInterestRate();
//
//            BigDecimal monthlyRate = annualRate.divide(
//                    BigDecimal.valueOf(1200),
//                    10,
//                    RoundingMode.HALF_UP
//            );
//
//            int remainingTenure = pendingSchedules.size();
//
//            BigDecimal newEmi;
//
//            if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
//
//                newEmi = newPrincipal.divide(
//                        BigDecimal.valueOf(remainingTenure),
//                        2,
//                        RoundingMode.HALF_UP
//                );
//
//            } else {
//
//                BigDecimal factor = BigDecimal.ONE.add(monthlyRate);
//
//                BigDecimal power = factor.pow(remainingTenure);
//
//                BigDecimal numerator = newPrincipal
//                        .multiply(monthlyRate)
//                        .multiply(power);
//
//                BigDecimal denominator = power
//                        .subtract(BigDecimal.ONE);
//
//                newEmi = numerator.divide(
//                        denominator,
//                        10,
//                        RoundingMode.HALF_UP
//                ).setScale(2, RoundingMode.HALF_UP);
//            }
//
//            BigDecimal openingBalance = newPrincipal;
//
//            for (int i = 0; i < pendingSchedules.size(); i++) {
//
//                EmiSchedules schedule = pendingSchedules.get(i);
//
//                BigDecimal interestAmount = openingBalance
//                        .multiply(monthlyRate)
//                        .setScale(2, RoundingMode.HALF_UP);
//
//                BigDecimal principalAmount = newEmi
//                        .subtract(interestAmount)
//                        .setScale(2, RoundingMode.HALF_UP);
//
//                BigDecimal closingBalance = openingBalance
//                        .subtract(principalAmount)
//                        .setScale(2, RoundingMode.HALF_UP);
//
//                if (i == pendingSchedules.size() - 1) {
//
//                    principalAmount = openingBalance;
//
//                    newEmi = principalAmount
//                            .add(interestAmount)
//                            .setScale(2, RoundingMode.HALF_UP);
//
//                    closingBalance = BigDecimal.ZERO
//                            .setScale(2, RoundingMode.HALF_UP);
//                }
//
//                schedule.setOpeningBalance(openingBalance);
//                schedule.setInterestAmount(interestAmount);
//                schedule.setPrincipalAmount(principalAmount);
//                schedule.setClosingBalance(closingBalance);
//                schedule.setEmi(newEmi);
//
//                openingBalance = closingBalance;
//            }
//
//            List<EmiSchedules> updatedSchedules =
//                    emiSchedulesRepo.saveAll(pendingSchedules);
//
////            return updatedSchedules.stream()
////                    .map(schedule ->
////                            modelMapper.map(
////                                    schedule,
////                                    EmiScheduleResponseDto.class
////                            )
////                    )
////                    .toList();
//
//            return updatedSchedules.stream()
//                    .map(this::mapToResponseDto)
//                    .toList();
//
//        }
//
//
//
//
//
//
//    @Override
//    public List<EmiScheduleResponseDto> generateEmi(Integer loanAccountId) {
//
//        LoanAccount account = loanAccountRepo.findById(loanAccountId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "Loan account not found with id: " + loanAccountId
//                        )
//                );
//
//        return generateEmiSchedule(account);
//    }
//
//
//
//
//
//    // copy pasted imte,zed breakdown
//
//
//
////    @Override
////    public EmiBreakdownDto getItemizedEmiBreakdown(Long emiScheduleId) {
////        EmiSchedules schedule = emiSchedulesRepo.findById(emiScheduleId)
////                .orElseThrow(() -> new ResourceNotFoundException("EMI Schedule not found: " + emiScheduleId));
////
////        BigDecimal bounceCharges = penaltyChargeRepository.findTotalAppliedPenaltiesByScheduleId(emiScheduleId)
////                .orElse(BigDecimal.ZERO);
////
////        BigDecimal lateInterest = interestAccrualRepository.findTotalAccruedInterestByScheduleId(emiScheduleId)
////                .orElse(BigDecimal.ZERO);
////
////        BigDecimal totalPayable = schedule.getEmi().add(bounceCharges).add(lateInterest);
////
////        return new EmiBreakdownDto(
////                schedule.getEmiScheduleId(),
////                schedule.getEmi(),
////                bounceCharges,
////                lateInterest,
////                totalPayable
////        );
////    }


    }
