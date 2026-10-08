package com.example.loanApplication.service;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.EligibilityRequest;
import com.example.loanApplication.dto.EligibilityResponse;

public interface EligibilityService {

    ResponseApi<EligibilityResponse> calculateEligibility(
            EligibilityRequest request
    );

    ResponseApi<EligibilityResponse> getLatestEligibility(
            Integer customerId
    );

}