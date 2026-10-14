package com.example.loanApplication.utils;

import com.example.loanApplication.entity.EmiSchedules;
import com.example.loanApplication.entity.InterestAccruals;
import com.example.loanApplication.entity.PenaltyCharges;
import com.example.loanApplication.repository.EmiSchedulesRepo;
import com.example.loanApplication.repository.InterestAccrualRepo;
import com.example.loanApplication.repository.PenaltyChargesRepo;
import com.example.loanApplication.service.NotificationService;
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

    private  final PenaltyChargesRepo penaltyChargesRepository;

    private  final InterestAccrualRepo interestAccrualRepo;
    private  final NotificationService notificationService;

    private  final  InterestAccrualRepo interestAccrualRepository;


    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void executeDailyPenaltyAndOverdueJob() {
        LocalDate today = LocalDate.now();
        log.info("Starting Daily Penalty & Overdue Job Scheduler for Date: {}", today);

        // 1. Fetch all PENDING EMI schedules where DueDate is before today
        List<EmiSchedules> overdueSchedules = emiSchedulesRepo.findByDueDateBeforeAndPaymentStatus(today, "PENDING");

        if (overdueSchedules.isEmpty()) {
            log.info("No overdue EMI schedules found for penalty processing today.");
            return;
        }

        log.info("Found {} overdue EMI schedules to process.", overdueSchedules.size());

        for (EmiSchedules schedule : overdueSchedules) {

            // 2. Apply ₹600 Bounce Charge (if not already applied for this schedule)
            boolean penaltyExists = penaltyChargesRepository.existsByEmiSchedule_EmiScheduleId(schedule.getEmiScheduleId());

            if (!penaltyExists) {
                PenaltyCharges penalty = new PenaltyCharges();
                penalty.setEmiSchedule(schedule);
                penalty.setLoanAccount(schedule.getLoanAccount());
                penalty.setPenaltyAmount(new BigDecimal("600.00")); // Flat ₹600 Bounce Penalty
                penalty.setReason("Auto-Applied Bounce Charge for Overdue EMI Schedule #" + schedule.getEmiScheduleId());
                penalty.setStatus("APPLIED");
                penalty.setCreatedAt(LocalDateTime.now());

                penaltyChargesRepository.save(penalty);
                log.info("Applied ₹600 Bounce Charge for EMI Schedule ID: {}", schedule.getEmiScheduleId());
            }

            // 3. Calculate Daily Late Fee (₹1 * Days Overdue)
            long daysOverdue = ChronoUnit.DAYS.between(schedule.getDueDate(), today);
            BigDecimal lateFeeAmount = new BigDecimal(daysOverdue); // ₹1 per day

            InterestAccruals accrual = new InterestAccruals();
            accrual.setLoanAccount(schedule.getLoanAccount());
            accrual.setInterestAmount(lateFeeAmount);
            accrual.setAccrualDate(LocalDateTime.now());
            accrual.setInstallmentNo(schedule.getInstallmentNo());
            accrual.setStatus("APPLIED");

            interestAccrualRepository.save(accrual);
            log.info("Recorded ₹{} late interest accrual for Schedule ID: {} (Overdue days: {})",
                    lateFeeAmount, schedule.getEmiScheduleId(), daysOverdue);

            // 4. Send Automated Overdue Notification Alert to Customer
            if (schedule.getLoanAccount() != null && schedule.getLoanAccount().getCustomerId() != null) {
                notificationService.sendNotification(
                        schedule.getLoanAccount().getCustomerId(),
                        "Your EMI due on " + schedule.getDueDate() + " is overdue by " + daysOverdue +
                                " days. Late charges of ₹600 bounce + ₹" + daysOverdue + " late fees have been applied.",
                        "Overdue EMI Payment Alert"
                );
            }
        }

        log.info("Completed Daily Penalty & Overdue Job Scheduler processing successfully.");
    }


}
