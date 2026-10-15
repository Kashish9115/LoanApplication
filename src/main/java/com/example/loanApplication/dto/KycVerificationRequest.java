package com.example.loanApplication.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class KycVerificationRequest {
    @NotBlank
    private String verificationStatus;
    @Size(max = 1000)
    private String rejectionReason;
}
