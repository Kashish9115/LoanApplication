package com.example.loanApplication.service;

import com.example.loanApplication.dto.LoanPaymentRequestDTO;
import com.example.loanApplication.dto.LoanPaymentResponseDTO;
import com.example.loanApplication.dto.RazorpayPaymentVerifyDto;

import java.math.BigDecimal;
import java.util.List;

public interface LoanPaymentService {


   LoanPaymentRequestDTO createRazorpayOrder(BigDecimal amount, String currency);
//
 LoanPaymentResponseDTO verifyAndProcessEmiPayment(RazorpayPaymentVerifyDto dto);
//
List<LoanPaymentResponseDTO> getPaymentHistoryByAccountId(Integer LoanAccountId);


}
