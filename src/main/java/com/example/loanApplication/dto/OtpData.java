package com.example.loanApplication.dto;

import lombok.Getter;

@Getter
public class OtpData {

    private final String code;

    private final long expiryTimestamp;

    public OtpData(
            String code,
            long validForSeconds
    ) {
        this.code = code;

        this.expiryTimestamp =
                System.currentTimeMillis()
                        + (validForSeconds * 1000);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTimestamp;
    }

    public boolean isValid(String inputCode) {
        return !isExpired()
                && this.code.equals(inputCode);
    }
}
