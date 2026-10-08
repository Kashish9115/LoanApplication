package com.example.loanApplication.service;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.LoanDealRequest;
import com.example.loanApplication.dto.LoanDealResponse;
import com.example.loanApplication.dto.PageResponse;

public interface LoanDealService {

    ResponseApi<LoanDealResponse> createLoanDeal(
            LoanDealRequest request
    );

    ResponseApi<LoanDealResponse> getLoanDealById(
            Integer dealId
    );

    ResponseApi<LoanDealResponse> getLatestLoanDealByCustomer(
            Integer customerId
    );

    ResponseApi<PageResponse<LoanDealResponse>> getLoanDealsByCustomer(
            Integer customerId,
            int page,
            int size
    );

}