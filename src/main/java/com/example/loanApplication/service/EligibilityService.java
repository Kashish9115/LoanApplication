package com.example.loanApplication.service;


import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.EligibilityResultDto;

import java.util.List;

public interface EligibilityService {

    ResponseApi<List<EligibilityResultDto>> getPendingCustomers();

    ResponseApi<EligibilityResultDto> checkEligibility(Integer customerId);

    ResponseApi<EligibilityResultDto> approveEligibility(Integer eligibilityId);

    ResponseApi<EligibilityResultDto> rejectEligibility(
            Integer eligibilityId,
            String reason
    );

    ResponseApi<EligibilityResultDto> getCustomerEligibility(
            Integer customerId
    );
}
