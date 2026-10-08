package com.example.loanApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupportTicketRequest {
    @NotNull
    private Integer customerId;
    private Integer loanAccountId;
    @NotBlank
    private String subject;
    @NotBlank
    private String description;
}
