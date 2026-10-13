package com.example.loanApplication.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegisterRequestDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Age is required")
    @Min(
            value = 21,
            message = "Age must be at least 21 years"
    )
    @Max(
            value = 60,
            message = "Age cannot exceed 60 years"
    )
    private Integer age;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            message = "Password must be at least 8 characters long"
    )
    private String password;

    /*
     * Mobile Number:
     * Exactly 10 digits starting with 6, 7, 8, or 9
     */
    @NotBlank(message = "Mobile number is required")
    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Mobile number must be a valid 10-digit Indian number"
    )
    private String mobileNo;

    /*
     * PAN Card:
     * 5 Uppercase Letters + 4 Digits + 1 Uppercase Letter
     *
     * Example: ABCDE1234F
     */
    @NotBlank(message = "PAN number is required")
    @Pattern(
            regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "Invalid PAN number format. Example: ABCDE1234F"
    )
    private String panNo;

    /*
     * Aadhaar Card:
     * 12 Digits starting with 2-9
     */
    @NotBlank(message = "Aadhaar number is required")
    @Pattern(
            regexp = "^[2-9][0-9]{11}$",
            message = "Invalid Aadhaar number format. Must be a 12-digit number"
    )
    private String aadhaarNo;

    @NotBlank(message = "Employment type is required")
    private String employmentType;

    @NotNull(message = "Monthly income is required")
    @Positive(
            message = "Monthly income must be greater than zero"
    )
    private BigDecimal monthlyIncome;

    @NotNull(message = "Monthly investment is required")
    @PositiveOrZero(
            message = "Monthly investment cannot be negative"
    )
    private BigDecimal monthlyInvestment;
}