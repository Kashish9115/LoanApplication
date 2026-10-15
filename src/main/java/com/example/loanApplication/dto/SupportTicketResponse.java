package com.example.loanApplication.dto;

import java.time.LocalDateTime;
import lombok.Data;

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
