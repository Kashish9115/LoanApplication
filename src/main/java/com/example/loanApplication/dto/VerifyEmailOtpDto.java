package com.example.loanApplication.dto;

import lombok.Data;

@Data
public class VerifyEmailOtpDto {
    private String email;
    private String otpCode;
}