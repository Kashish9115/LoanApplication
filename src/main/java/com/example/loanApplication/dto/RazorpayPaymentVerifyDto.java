package com.example.loanApplication.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RazorpayPaymentVerifyDto {



    // Razorpay Checkout Verification Attributes
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    // Domain & Application Context Attributes
    private Long loanAccountId;
    private Long emiScheduleId;
    private BigDecimal paymentAmount;
}
