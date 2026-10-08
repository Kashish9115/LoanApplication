package com.example.loanApplication.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SupportTicketResponse {
    private Integer ticketId;
    private Integer customerId;
    private Integer loanAccountId;
    private String subject;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
