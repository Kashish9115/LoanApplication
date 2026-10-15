package com.example.loanApplication.service;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.*;
import com.example.loanApplication.entity.ForeClosureRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ForeClosureService {
    ResponseApi<List<Integer>> getAllActiveLoanAccounts(Integer customerId);

    ResponseApi<LoanAccountDetailsDto> getLoanAccountDetails(Integer loanAccountId);

    ResponseApi<ForeClosureRequest> requestFullClosure(
            Integer loanAccountId,
            FullClosureRequestDto request
    );
    ResponseApi<ForeClosureRequest> requestPartialClosure(
            Integer loanAccountId,
            PartialClosureRequestDto request
    );
    ResponseApi<List<CustomerForeClosureRequestDto>>
    getCustomerForeclosureRequests(Integer customerId);

    ResponseApi<List<ForeClosureRequest>>
    getPendingForeclosureRequests();

    ResponseApi<ForeClosureRequest> approveForeclosureRequest(
            Integer requestId);

    ResponseApi<ForeClosureRequest> rejectForeclosureRequest(
            Integer requestId);
    void doneClosure(Integer requestId, BigDecimal paidAmount);

    ResponseApi<List<ForeClosurePaymentDto>>
    getApprovedForeclosurePayments(Integer customerId);

//    LoanPaymentResponseDTO processForeClosurePayment(
//            ForeClosurePaymentRequestDto dto);
    ResponseApi<Page<CustomerForeClosureRequestDto>>
    getCustomerForeclosureRequests(
            Integer customerId,
            Pageable pageable
    );
}
