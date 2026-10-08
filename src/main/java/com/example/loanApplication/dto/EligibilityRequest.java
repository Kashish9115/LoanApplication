package com.example.loanApplication.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityRequest {

    @NotNull(message = "Customer ID is required")
    private Integer customerId;

    @NotNull(message = "CIBIL score is required")
    @Min(value = 300, message = "CIBIL score cannot be less than 300")
    @Max(value = 900, message = "CIBIL score cannot be greater than 900")
    private Integer cibilScore;
}