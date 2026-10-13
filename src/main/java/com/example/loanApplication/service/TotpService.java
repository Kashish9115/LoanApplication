package com.example.loanApplication.service;

public interface TotpService {

    // Unique 2FA Secret Key generate karega
    String generateSecretKey();

    // User ke enter kiye gaye 6-digit TOTP code ko verify karega
    boolean verifyTotp(
            String secretKey,
            String totpCode
    );
}