package com.example.loanApplication.service;

public interface EmailService {

    void sendEmail(String to, String subject, String message);
    void sendOtpEmail(String toEmail, String otpCode);
}