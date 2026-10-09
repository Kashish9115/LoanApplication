package com.example.loanApplication.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otpCode);
}
