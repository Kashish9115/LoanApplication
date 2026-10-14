package com.example.loanApplication.service;

import com.example.loanApplication.dto.RazorpayOrderResponseDto;

public interface RazorpayService {


    RazorpayOrderResponseDto createOrderForEmiSchedule(Integer emiScheduleId);


  //  RazorpayOrderResponseDto createOrder(BigDecimal amount, String currency);


    boolean verifySignature(String orderId, String paymentId, String signature);






    RazorpayOrderResponseDto createOrderForOverdueEmis(
            Integer loanAccountId
    );
}
