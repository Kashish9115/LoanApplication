package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.NotificationResponseDto;
import com.example.loanApplication.service.NotificationService;

public class NotificationServiceImpl implements NotificationService {


    @Override
    public NotificationResponseDto sendNotification(Long CustomerId, String message, String emailSubject) {
        return null;
    }

    @Override
    public NotificationResponseDto getCustomerNotifications(Long customerId) {
        return null;
    }

    @Override
    public NotificationResponseDto markNotificationAsRead(Long notificationId) {
        return null;
    }

    @Override
    public void sendEmiDueReminders() {

    }
}
