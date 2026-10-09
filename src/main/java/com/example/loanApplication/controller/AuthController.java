package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.*;
import com.example.loanApplication.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ResponseApi<String>> register(
            @Valid @RequestBody RegisterRequestDto request
    ) {

        String message = authService.registerCustomer(request);

        ResponseApi<String> response = ResponseApi.<String>builder()
                .success(true)
                .message(message)
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    // 2. Email OTP Verification
    @PostMapping("/verify-email-otp")
    public ResponseEntity<ResponseApi<String>> verifyEmailOtp(
            @RequestBody VerifyEmailOtpDto request
    ) {
        authService.verifyEmailOtp(request);

        ResponseApi<String> response = ResponseApi.<String>builder()
                .success(true)
                .message("Email verified successfully.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    // 3. Login Option 1: Email + Password
    @PostMapping("/login")
    public ResponseEntity<ResponseApi<AuthResponseDto>> login(
            @RequestBody LoginRequestDto request
    ) {
        AuthResponseDto authData = authService.login(request);

        ResponseApi<AuthResponseDto> response =
                ResponseApi.<AuthResponseDto>builder()
                        .success(true)
                        .message(
                                "Primary credentials verified. "
                                        + "Please complete 2FA verification."
                        )
                        .data(authData)
                        .build();

        return ResponseEntity.ok(response);
    }

    // 4. Login Option 2: Google OAuth
    @PostMapping("/google-login")
    public ResponseEntity<ResponseApi<AuthResponseDto>> googleLogin(
            @RequestBody GoogleLoginDto request
    ) {
        AuthResponseDto authData = authService.googleLogin(request);

        ResponseApi<AuthResponseDto> response =
                ResponseApi.<AuthResponseDto>builder()
                        .success(true)
                        .message(
                                "Google authentication passed. "
                                        + "Please complete 2FA verification."
                        )
                        .data(authData)
                        .build();

        return ResponseEntity.ok(response);
    }

    // 5. Phase 2: 2FA TOTP Verification
    @PostMapping("/verify-2fa")
    public ResponseEntity<ResponseApi<AuthResponseDto>> verify2FA(
            @RequestBody Verify2faDto request
    ) {
        AuthResponseDto authData = authService.verify2FA(request);

        ResponseApi<AuthResponseDto> response =
                ResponseApi.<AuthResponseDto>builder()
                        .success(true)
                        .message(
                                "2FA verification successful. "
                                        + "Authentication complete."
                        )
                        .data(authData)
                        .build();

        return ResponseEntity.ok(response);
    }

    // 6. Refresh Token
    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseApi<AuthResponseDto>> refreshToken(
            @RequestParam("token") String refreshToken
    ) {
        AuthResponseDto authData =
                authService.refreshToken(refreshToken);

        ResponseApi<AuthResponseDto> response =
                ResponseApi.<AuthResponseDto>builder()
                        .success(true)
                        .message("Access token refreshed successfully.")
                        .data(authData)
                        .build();

        return ResponseEntity.ok(response);
    }

    // 7. Logout
    @PostMapping("/logout")
    public ResponseEntity<ResponseApi<String>> logout(
            @RequestParam("token") String refreshToken
    ) {
        authService.logout(refreshToken);

        ResponseApi<String> response = ResponseApi.<String>builder()
                .success(true)
                .message("Logged out successfully.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }
}
