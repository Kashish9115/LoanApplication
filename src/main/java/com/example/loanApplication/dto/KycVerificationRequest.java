package com.example.loanApplication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KycVerificationRequest {
    @NotBlank
    private String verificationStatus;
}
