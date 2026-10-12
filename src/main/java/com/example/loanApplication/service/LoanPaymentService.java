package com.example.loanApplication.service;

import com.example.loanApplication.dto.LoanPaymentRequestDTO;
import com.example.loanApplication.dto.LoanPaymentResponseDTO;

import java.util.List;

public interface LoanPaymentService {


   //LoanPaymentRequestDTO createRazorpayOrder(BigDecimal amount, String currency);
//
 //LoanPaymentResponseDTO verifyAndProcessEmiPayment(RazorpayPaymentVerifyDto dto);
//this is use full
List<LoanPaymentResponseDTO> getPaymentHistoryByAccountId(Integer LoanAccountId);

// new method
LoanPaymentResponseDTO  processPayment(LoanPaymentRequestDTO loanPaymentRequestDTO);
}
