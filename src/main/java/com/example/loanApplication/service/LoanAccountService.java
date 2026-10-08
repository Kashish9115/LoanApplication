package com.example.loanApplication.service;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.LoanAccountResponse;
import com.example.loanApplication.dto.PageResponse;

public interface LoanAccountService {

    ResponseApi<LoanAccountResponse> createLoanAccount(
            Integer dealId
    );

    ResponseApi<LoanAccountResponse> getLoanAccountById(
            Integer loanAccountId
    );

    ResponseApi<LoanAccountResponse> getLoanAccountByDealId(
            Integer dealId
    );

    ResponseApi<LoanAccountResponse> getLatestLoanAccountByCustomerId(
            Integer customerId
    );

    ResponseApi<PageResponse<LoanAccountResponse>> getLoanAccountsByCustomerId(
            Integer customerId,
            int page,
            int size
    );
}
