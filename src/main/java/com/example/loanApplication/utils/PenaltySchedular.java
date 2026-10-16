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
