package com.example.loanApplication.utils;

import com.example.loanApplication.entity.EmiSchedules;
import com.example.loanApplication.entity.InterestAccruals;
import com.example.loanApplication.entity.PenaltyCharges;
import com.example.loanApplication.repository.EmiSchedulesRepo;
import com.example.loanApplication.repository.InterestAccrualRepo;
import com.example.loanApplication.repository.PenaltyChargesRepo;
import com.example.loanApplication.service.NotificationService;
import com.example.loanApplication.service.PenaltyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Component
public class PenaltySchedular {

    private  final EmiSchedulesRepo emiSchedulesRepo;
    private  final PenaltyService penaltyService;



private static final BigDecimal BOUNCE_CHARGE =
        new BigDecimal("600.00");

    private static final BigDecimal DAILY_LATE_FEE =
            new BigDecimal("1.00");




//    @Scheduled(cron = "0 0 0 * * ?")
//    @Transactional
//    public void executeDailyPenaltyAndOverdueJob() {
//
//        LocalDate today = LocalDate.now();
//
//        log.info("Starting overdue EMI job for {}", today);
//
//        List<EmiSchedules> overdueSchedules =
//                emiSchedulesRepo.findByDueDateBeforeAndPaymentStatus(
//                        today, "PENDING"
//                );
//
//        for (EmiSchedules schedule : overdueSchedules) {
//
//            if (schedule.getLoanAccount() == null
//                    || schedule.getDueDate() == null
//                    || schedule.getInstallmentNo() == null) {
//                log.warn("Skipping invalid EMI schedule: {}",
//                        schedule.getEmiScheduleId());
//                continue;
//            }
//
//            Integer scheduleId = schedule.getEmiScheduleId();
//            Integer loanAccountId =
//                    schedule.getLoanAccount().getLoanAccountId();
//            Integer installmentNo = schedule.getInstallmentNo();
//
//            // 1. Apply ₹600 only once per overdue EMI.
//            boolean penaltyExists =
//                    penaltyChargesRepo
//                            .existsByEmiSchedule_EmiScheduleId(scheduleId);
//
//            if (!penaltyExists) {
//
//                PenaltyCharges penalty = new PenaltyCharges();
//                penalty.setEmiSchedule(schedule);
//                penalty.setLoanAccount(schedule.getLoanAccount());
//                penalty.setPenaltyAmount(BOUNCE_CHARGE);
//                penalty.setReason("Bounce Charge - Overdue EMI");
//                penalty.setStatus("APPLIED");
//                penalty.setCreatedAt(LocalDateTime.now());
//
//                penaltyChargesRepo.save(penalty);
//
//                log.info("₹600 bounce charge applied to EMI {}", scheduleId);
//            }
//
//            // 2. Record ₹1 for each overdue calendar day.
//            LocalDate accrualDate = schedule.getDueDate().plusDays(1);
//            boolean todayAccrualCreated = false;
//
//            while (!accrualDate.isAfter(today)) {
//
//                LocalDateTime start = accrualDate.atStartOfDay();
//                LocalDateTime end = accrualDate.plusDays(1)
//                        .atStartOfDay().minusNanos(1);
//
//                boolean exists =
//                        interestAccrualRepo
//                                .existsByLoanAccountIdAndInstallmentNoAndAccrualDateBetween(
//                                        loanAccountId,
//                                        installmentNo,
//                                        start,
//                                        end
//                                );
//
//                if (!exists) {
//                    InterestAccruals accrual = new InterestAccruals();
//                    accrual.setLoanAccountId(loanAccountId);
//                    accrual.setInstallmentNo(installmentNo);
//                    accrual.setInterestAmount(DAILY_LATE_FEE);
//                    accrual.setAccrualDate(accrualDate.atTime(0, 0));
//                    accrual.setStatus("APPLIED");
//
//                    interestAccrualRepo.save(accrual);
//
//                    if (accrualDate.equals(today)) {
//                        todayAccrualCreated = true;
//                    }
//
//                    log.info("₹1 late fee recorded for EMI {}, date {}",
//                            scheduleId, accrualDate);
//                }
//
//                accrualDate = accrualDate.plusDays(1);
//            }
//
//            // 3. Send notification when today's late fee is newly recorded.
//            if (todayAccrualCreated) {
//
//                Integer customerId =
//                        schedule.getLoanAccount().getCustomerId();
//
//                if (customerId != null) {
//
//                    long daysOverdue = ChronoUnit.DAYS.between(
//                            schedule.getDueDate(), today
//                    );
//
//                    BigDecimal totalLateFee =
//                            DAILY_LATE_FEE.multiply(
//                                    BigDecimal.valueOf(daysOverdue)
//                            );
//
//                    notificationService.sendNotification(
//                            customerId,
//                            "Your EMI installment #"
//                                    + installmentNo
//                                    + " is overdue by "
//                                    + daysOverdue
//                                    + " days. Bounce charge: ₹600.00. "
//                                    + "Accumulated late fee: ₹"
//                                    + totalLateFee
//                                    + ". Please pay your overdue EMI.",
//                            "Overdue EMI Payment Alert"
//                    );
//                }
//            }
//        }
//
//        log.info("Overdue EMI job completed.");
//    }




    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
    public void executeDailyPenaltyAndOverdueJob() {

        LocalDate today = LocalDate.now();

        log.info("Starting overdue EMI processing for {}", today);

        List<EmiSchedules> overdueSchedules =
                emiSchedulesRepo.findByDueDateBeforeAndPaymentStatus(
                        today, "PENDING"
                );

        for (EmiSchedules schedule : overdueSchedules) {
            penaltyService.processOverdueEmi(schedule, today);
        }

        log.info(
                "Overdue EMI processing completed. Total EMIs: {}",
                overdueSchedules.size()
        );
    }

}
