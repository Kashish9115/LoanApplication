package com.example.loanApplication.service;

import com.example.loanApplication.dto.ForeClosurePaymentRequestDto;
import com.example.loanApplication.dto.LoanPaymentRequestDTO;
import com.example.loanApplication.dto.LoanPaymentResponseDTO;

import java.util.List;

public interface LoanPaymentService {



List<LoanPaymentResponseDTO> getPaymentHistoryByAccountId(Integer LoanAccountId);



   LoanPaymentResponseDTO processPaymentLedger(LoanPaymentRequestDTO dto);


     List<LoanPaymentResponseDTO> getAllPaymentsForAdmin();




    LoanPaymentResponseDTO processForeClosurePayment(
            ForeClosurePaymentRequestDto dto);

}
