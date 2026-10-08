package com.example.loanApplication.service;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.SanctionResponse;

public interface SanctionService {

    ResponseApi<SanctionResponse> sanctionLoan(
            Integer dealId
    );

    ResponseApi<SanctionResponse> getSanctionByDealId(
            Integer dealId
    );

    ResponseApi<SanctionResponse> getSanctionById(
            Integer sanctionId
    );

}