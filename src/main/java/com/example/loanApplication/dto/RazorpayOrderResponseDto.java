package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayOrderResponseDto {



    // Razorpay Checkout Verification Attributes
    private String razorpayOrderId;
//    private String razorpayPaymentId;
//    private String razorpaySignature;

    // Domain & Application Context Attributes
    private String currency;
    private BigDecimal amount;
}
