package com.example.loanApplication.service;

import com.example.loanApplication.dto.*;

public interface LoanPaymentService {






   LoanPaymentResponseDTO processPaymentLedger(LoanPaymentRequestDTO dto);


     PageResponse<PaymentHistoryResponseDTO> getAllPaymentsForAdmin(int page, int size);


    PageResponse<PaymentHistoryResponseDTO> getPaymentHistoryByAccountId(
            Integer loanAccountId, int page, int size);


    LoanPaymentResponseDTO processForeClosurePayment(
            ForeClosurePaymentRequestDto dto);

}
