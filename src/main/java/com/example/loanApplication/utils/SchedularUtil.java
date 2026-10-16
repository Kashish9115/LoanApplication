package com.example.loanApplication.utils;

import com.example.loanApplication.dto.EmiReminderDto;

import com.example.loanApplication.repository.EmiSchedulesRepo;
import com.example.loanApplication.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
@Component
@RequiredArgsConstructor
public class SchedularUtil {


    private final EmiSchedulesRepo emiScheduleRepository;
    private final NotificationService notificationService;


    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(fixedRate = 10000)
    public void sendEmiDueReminders() {
        LocalDate reminderTargetDate = LocalDate.now().plusDays(3);


        List<EmiReminderDto> upcomingEmis = emiScheduleRepository.findEmiReminders(reminderTargetDate, "PENDING");

        for (EmiReminderDto emi : upcomingEmis) {
            Integer customerId = emi.getCustomerId();
            String message = "Reminder: Your EMI installment #" + emi.getInstallmentNo() +
                    " of ₹" + emi.getEmi() + " is due on " + emi.getDueDate() +
                    ". Please maintain sufficient account balance to avoid bounce charges.";

            notificationService.sendNotification(
                    customerId,
                    message,
                    "EMI Payment Due Reminder"
            );
        }


    }
}
