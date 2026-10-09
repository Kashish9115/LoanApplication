package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String preAuthToken;// Issued in Phase 1 (Credentials / Google Login)
    private String accessToken;// Issued after Phase 2 (2FA OTP Verification)
    private String refreshToken;// Issued after Phase 2 (2FA OTP Verification)
    private String roleName;
    private String email;

}

