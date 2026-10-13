package com.example.loanApplication.service;

import com.example.loanApplication.dto.*;

public interface AuthService {

    String registerCustomer(RegisterRequestDto request);

    boolean verifyEmailOtp(VerifyEmailOtpDto request);

    AuthResponseDto login(LoginRequestDto request);

    AuthResponseDto googleLogin(GoogleLoginDto request);

    AuthResponseDto verify2FA(Verify2faDto request);

    AuthResponseDto refreshToken(String refreshToken);

    void logout(String refreshToken);
}
