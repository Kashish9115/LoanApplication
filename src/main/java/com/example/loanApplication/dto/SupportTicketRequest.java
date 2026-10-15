package com.example.loanApplication.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Data
public class SupportTicketRequest {
    @NotNull @Positive private Integer customerId;
    @Positive private Integer loanAccountId;
    @NotBlank @Size(max = 255) private String subject;
    @NotBlank @Size(max = 2000) private String description;
}
