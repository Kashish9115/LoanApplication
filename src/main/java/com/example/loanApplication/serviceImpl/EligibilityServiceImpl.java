
package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.EligibilityRequest;
import com.example.loanApplication.dto.EligibilityResponse;
import com.example.loanApplication.entity.EligibilityResult;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.EligibilityResultRepository;
import com.example.loanApplication.service.EligibilityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityServiceImpl implements EligibilityService {

    private final EligibilityResultRepository eligibilityResultRepository;

    @Override
    @Transactional
    public ResponseApi<EligibilityResponse> calculateEligibility(
            EligibilityRequest request
    ) {

        Integer customerId = request.getCustomerId();
        Integer cibilScore = request.getCibilScore();

        log.info(
                "Starting eligibility calculation for customerId: {}",
                customerId
        );

        log.info("CIBIL score received for eligibility assessment");

        boolean eligible;
        BigDecimal eligibleLoanAmount;
        String cibilStatus;
        String rejectionReason = null;

        if (cibilScore >= 900) {

            eligible = true;
            cibilStatus = "EXCELLENT";
            eligibleLoanAmount = new BigDecimal("10000000");

        } else if (cibilScore >= 800) {

            eligible = true;
            cibilStatus = "VERY_GOOD";
            eligibleLoanAmount = new BigDecimal("10000000");

        } else if (cibilScore >= 750) {

            eligible = true;
            cibilStatus = "GOOD";
            eligibleLoanAmount = new BigDecimal("7500000");

        } else if (cibilScore >= 700) {

            eligible = true;
            cibilStatus = "AVERAGE";
            eligibleLoanAmount = new BigDecimal("5000000");

        } else if (cibilScore >= 650) {

            eligible = true;
            cibilStatus = "RISKY";
            eligibleLoanAmount = new BigDecimal("2500000");

        } else {

            eligible = false;
            cibilStatus = "REJECTED";
            eligibleLoanAmount = BigDecimal.ZERO;

            rejectionReason =
                    "Customer is not eligible because CIBIL score is below 650.";
        }

        log.info(
                "Eligibility assessment completed for customerId: {}, status: {}, eligible: {}",
                customerId,
                cibilStatus,
                eligible
        );

        EligibilityResult eligibilityResult =
                EligibilityResult.builder()
                        .customerId(customerId)
                        .cibilScore(cibilScore)
                        .isEligible(eligible)
                        .loanAmount(eligibleLoanAmount)
                        .rejectionReason(rejectionReason)
                        .build();

        EligibilityResult savedEligibility =
                eligibilityResultRepository.save(eligibilityResult);

        log.info(
                "Eligibility result saved successfully. eligibilityId: {}, customerId: {}",
                savedEligibility.getEligibilityId(),
                customerId
        );

        EligibilityResponse response =
                EligibilityResponse.builder()
                        .eligibilityId(savedEligibility.getEligibilityId())
                        .customerId(savedEligibility.getCustomerId())
                        .cibilScore(savedEligibility.getCibilScore())
                        .cibilStatus(cibilStatus)
                        .eligible(savedEligibility.getIsEligible())
                        .eligibleLoanAmount(savedEligibility.getLoanAmount())
                        .rejectionReason(savedEligibility.getRejectionReason())
                        .build();

        return ResponseApi.<EligibilityResponse>builder()
                .success(true)
                .message(
                        eligible
                                ? "Eligibility calculated successfully"
                                : "Customer is not eligible for loan"
                )
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<EligibilityResponse> getLatestEligibility(
            Integer customerId
    ) {

        log.info(
                "Fetching latest eligibility result for customerId: {}",
                customerId
        );

        EligibilityResult eligibilityResult =
                eligibilityResultRepository
                        .findTopByCustomerIdOrderByEligibilityIdDesc(customerId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Eligibility result not found for customerId: {}",
                                    customerId
                            );

                            return new ResourceNotFoundException(
                                    "Eligibility result not found for customer: "
                                            + customerId
                            );
                        });

        String cibilStatus =
                getCibilStatus(eligibilityResult.getCibilScore());

        EligibilityResponse response =
                EligibilityResponse.builder()
                        .eligibilityId(eligibilityResult.getEligibilityId())
                        .customerId(eligibilityResult.getCustomerId())
                        .cibilScore(eligibilityResult.getCibilScore())
                        .cibilStatus(cibilStatus)
                        .eligible(eligibilityResult.getIsEligible())
                        .eligibleLoanAmount(eligibilityResult.getLoanAmount())
                        .rejectionReason(eligibilityResult.getRejectionReason())
                        .build();

        log.info(
                "Latest eligibility result fetched successfully. eligibilityId: {}, customerId: {}",
                eligibilityResult.getEligibilityId(),
                customerId
        );

        return ResponseApi.<EligibilityResponse>builder()
                .success(true)
                .message("Eligibility fetched successfully")
                .data(response)
                .build();
    }

    private String getCibilStatus(Integer cibilScore) {

        if (cibilScore >= 900) {
            return "EXCELLENT";
        }

        if (cibilScore >= 800) {
            return "VERY_GOOD";
        }

        if (cibilScore >= 750) {
            return "GOOD";
        }

        if (cibilScore >= 700) {
            return "AVERAGE";
        }

        if (cibilScore >= 650) {
            return "RISKY";
        }

        return "REJECTED";
    }
}
