package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {

    private Integer notificationId;
    private Integer customerId;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}