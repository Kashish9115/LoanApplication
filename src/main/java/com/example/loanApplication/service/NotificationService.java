package com.example.loanApplication.service;

import com.example.loanApplication.dto.NotificationResponseDto;

public interface NotificationService {

NotificationResponseDto sendNotification(Long CustomerId, String message, String emailSubject );

NotificationResponseDto getCustomerNotifications(Long customerId);

NotificationResponseDto  markNotificationAsRead(Long notificationId);


 void sendEmiDueReminders();

}
