package com.example.loanApplication.dto;

import lombok.Data;

@Data
public class Verify2faDto {
    private String preAuthToken;
    private String totpCode;
}