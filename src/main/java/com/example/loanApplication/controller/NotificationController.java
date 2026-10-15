package com.example.loanApplication.controller;

import com.example.loanApplication.dto.NotificationResponseDto;
import com.example.loanApplication.serviceImpl.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {


    private final NotificationServiceImpl notificationService;


    @GetMapping("/{customerId}")
    public ResponseEntity<List<NotificationResponseDto>> getCustomerNotifications(@PathVariable Integer customerId) {
        List<NotificationResponseDto> notifications = notificationService.getCustomerNotifications(customerId);
        return ResponseEntity.ok(notifications);
    }


    @PostMapping("/mark-read/{notificationId}")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Integer notificationId) {
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read successfully.");
    }











}
