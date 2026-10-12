package com.example.loanApplication.service;

import com.example.loanApplication.dto.RazorpayOrderResponseDto;

import java.math.BigDecimal;

public interface RazorpayService {



    /**
     * Communicates with Razorpay REST API to generate a payment order_id.
     */
    RazorpayOrderResponseDto createOrder(BigDecimal amount, String currency);

    /**
     * Cryptographically validates HMAC-SHA256 payment signature returned by frontend modal.
     */
    boolean verifySignature(String orderId, String paymentId, String signature);
}
