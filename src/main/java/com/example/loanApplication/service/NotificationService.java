package com.example.loanApplication.service;

import com.example.loanApplication.dto.NotificationResponseDto;

import java.util.List;

public interface NotificationService {

NotificationResponseDto sendNotification(Integer CustomerId, String message, String emailSubject );

List<NotificationResponseDto> getCustomerNotifications(Integer customerId);

void  markNotificationAsRead(Integer notificationId);




}
