package com.example.loanApplication.service;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.DisbursementRequest;
import com.example.loanApplication.dto.DisbursementResponse;
import com.example.loanApplication.dto.PageResponse;

public interface DisbursementService {

    ResponseApi<DisbursementResponse> createDisbursement(
            DisbursementRequest request
    );

    ResponseApi<DisbursementResponse> completeDisbursement(
            Integer disbursementId
    );

    ResponseApi<DisbursementResponse> getDisbursementById(
            Integer disbursementId
    );

    ResponseApi<DisbursementResponse> getLatestDisbursementByDealId(
            Integer dealId
    );

    ResponseApi<PageResponse<DisbursementResponse>> getDisbursementsByDealId(
            Integer dealId,
            int page,
            int size
    );

}