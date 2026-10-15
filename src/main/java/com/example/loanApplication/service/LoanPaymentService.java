package com.example.loanApplication.service;

import com.example.loanApplication.dto.ForeClosurePaymentRequestDto;
import com.example.loanApplication.dto.LoanPaymentRequestDTO;
import com.example.loanApplication.dto.LoanPaymentResponseDTO;
import com.example.loanApplication.dto.PageResponse;
import org.hibernate.query.Page;

import java.util.List;

public interface LoanPaymentService {






   LoanPaymentResponseDTO processPaymentLedger(LoanPaymentRequestDTO dto);


     PageResponse<LoanPaymentResponseDTO> getAllPaymentsForAdmin(int page, int size);

    // With pagination
    PageResponse<LoanPaymentResponseDTO> getPaymentHistoryByAccountId(
            Integer loanAccountId, int page, int size);


    LoanPaymentResponseDTO processForeClosurePayment(
            ForeClosurePaymentRequestDto dto);

}
