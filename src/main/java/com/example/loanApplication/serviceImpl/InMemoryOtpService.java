package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.OtpData;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryOtpService {

    // In-memory thread-safe map:
    // email -> OtpData
    private final Map<String, OtpData> otpMap =
            new ConcurrentHashMap<>();

    // 1. Save OTP with 5 minutes (300 seconds) validity
    public String generateAndSaveOtp(String email) {

        String generatedOtp =
                String.valueOf(
                        (int) ((Math.random() * 900000) + 100000)
                );

        otpMap.put(
                email.toLowerCase(),
                new OtpData(generatedOtp, 300)
        );

        // 300 seconds = 5 minutes
        return generatedOtp;
    }

    // 2. Validate OTP using OtpData helper methods
    public boolean validateOtp(
            String email,
            String inputOtp
    ) {

        OtpData otpData =
                otpMap.get(email.toLowerCase());

        if (otpData == null
                || !otpData.isValid(inputOtp)) {

            return false;
        }

        // Verification success -> clear OTP from memory
        otpMap.remove(email.toLowerCase());

        return true;
    }
}