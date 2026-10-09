package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.service.EmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(
            String toEmail,
            String otpCode
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(toEmail);

        message.setSubject(
                "LoanApp - Email Verification OTP"
        );

        String emailBody =
                "Hello,\n\n"
                        + "Your OTP for email verification is: "
                        + otpCode
                        + "\n"
                        + "(This code is valid for 5 minutes.)"
                        + "\n\n"
                        + "Regards,\n"
                        + "LoanApp Security Team";

        message.setText(emailBody);

        mailSender.send(message);
    }
}