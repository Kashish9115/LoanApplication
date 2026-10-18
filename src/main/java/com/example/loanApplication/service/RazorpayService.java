package com.example.loanApplication.service;

import com.example.loanApplication.dto.RazorpayOrderResponseDto;

public interface RazorpayService {


    RazorpayOrderResponseDto createOrderForEmiSchedule(Integer emiScheduleId);


  //  RazorpayOrderResponseDto createOrder(BigDecimal amount, String currency);


  //  boolean verifySignature(String orderId, String paymentId, String signature);

    boolean verifySignature(String orderId, String paymentId, String signature);

    boolean verifyCapturedPayment(String orderId, String paymentId, String signature, java.math.BigDecimal expectedAmount);




    RazorpayOrderResponseDto createOrderForOverdueEmis(
            Integer loanAccountId
    );
}
