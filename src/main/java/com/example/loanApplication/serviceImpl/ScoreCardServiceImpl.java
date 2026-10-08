package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.ScoreCardResponse;
import com.example.loanApplication.entity.EligibilityResult;
import com.example.loanApplication.entity.ScoreCard;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.EligibilityResultRepository;
import com.example.loanApplication.repository.ScoreCardRepository;
import com.example.loanApplication.service.ScoreCardService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScoreCardServiceImpl implements ScoreCardService {

    private final EligibilityResultRepository eligibilityResultRepository;

    private final ScoreCardRepository scoreCardRepository;


    @Override
    @Transactional
    public ResponseApi<ScoreCardResponse> generateScoreCard(
            Integer customerId
    ) {


        /*
         * Step 1:
         * Get latest eligibility result of customer.
         */
        EligibilityResult eligibilityResult =
                eligibilityResultRepository
                        .findTopByCustomerIdOrderByEligibilityIdDesc(customerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Eligibility result not found for customer: "
                                                + customerId
                                )
                        );


        Integer cibilScore =
                eligibilityResult.getCibilScore();


        boolean eligible =
                Boolean.TRUE.equals(
                        eligibilityResult.getIsEligible()
                );


        String cibilStatus =
                getCibilStatus(cibilScore);


        String riskCategory =
                cibilStatus;


        String approvalType;

        String currentStatus;

        String rejectionReason =
                eligibilityResult.getRejectionReason();


        /*
         * Step 2:
         * Decide approval type.
         *
         * Rule:
         *
         * CIBIL > 800
         *      → AUTO_APPROVED
         *
         * CIBIL 650 - 800
         *      → MANUAL_REVIEW
         *
         * CIBIL < 650
         *      → REJECTED
         */
        if (!eligible || cibilScore < 650) {

            approvalType = "REJECTED";

            currentStatus = "REJECTED";


            if (rejectionReason == null) {

                rejectionReason =
                        "Customer is not eligible for loan.";
            }

        } else if (cibilScore > 800) {

            approvalType = "AUTO_APPROVAL";

            currentStatus = "AUTO_APPROVED";

        } else {

            approvalType = "MANUAL_REVIEW";

            currentStatus = "MANUAL_REVIEW";
        }


        /*
         * Step 3:
         * Create ScoreCard entity.
         */
        ScoreCard scoreCard =
                ScoreCard.builder()
                        .customerId(
                                eligibilityResult.getCustomerId()
                        )
                        .currentStatus(
                                currentStatus
                        )
                        .rejectionReason(
                                rejectionReason
                        )
                        .appliedDate(
                                LocalDateTime.now()
                        )
                        .cibilScore(
                                cibilScore
                        )
                        .riskCategory(
                                riskCategory
                        )
                        .eligibleLoanAmount(
                                eligibilityResult.getLoanAmount()
                        )
                        .build();


        /*
         * Step 4:
         * Save ScoreCard in database.
         */
        ScoreCard savedScoreCard =
                scoreCardRepository.save(scoreCard);


        /*
         * Step 5:
         * Convert entity to response DTO.
         */
        ScoreCardResponse response =
                ScoreCardResponse.builder()
                        .scoreCardId(
                                savedScoreCard.getScoreCardId()
                        )
                        .customerId(
                                savedScoreCard.getCustomerId()
                        )
                        .cibilScore(
                                savedScoreCard.getCibilScore()
                        )
                        .cibilStatus(
                                cibilStatus
                        )
                        .riskCategory(
                                savedScoreCard.getRiskCategory()
                        )
                        .eligibleLoanAmount(
                                savedScoreCard.getEligibleLoanAmount()
                        )
                        .approvalType(
                                approvalType
                        )
                        .currentStatus(
                                savedScoreCard.getCurrentStatus()
                        )
                        .rejectionReason(
                                savedScoreCard.getRejectionReason()
                        )
                        .appliedDate(
                                savedScoreCard.getAppliedDate()
                        )
                        .build();


        /*
         * Step 6:
         * Return common response wrapper.
         */
        return ResponseApi
                .<ScoreCardResponse>builder()
                .success(true)
                .message(
                        currentStatus.equals("REJECTED")
                                ? "Scorecard generated. Customer is not eligible for loan."
                                : "Scorecard generated successfully"
                )
                .data(response)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseApi<ScoreCardResponse> getLatestScoreCard(
            Integer customerId
    ) {

        /*
         * Step 1:
         * Get latest scorecard.
         */
        ScoreCard scoreCard =
                scoreCardRepository
                        .findTopByCustomerIdOrderByScoreCardIdDesc(
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Scorecard not found for customer: "
                                                + customerId
                                )
                        );


        /*
         * Step 2:
         * Recreate display values from stored CIBIL score/status.
         */
        String cibilStatus =
                getCibilStatus(
                        scoreCard.getCibilScore()
                );


        String approvalType =
                getApprovalType(
                        scoreCard.getCibilScore()
                );


        /*
         * Step 3:
         * Create response DTO.
         */
        ScoreCardResponse response =
                ScoreCardResponse.builder()
                        .scoreCardId(
                                scoreCard.getScoreCardId()
                        )
                        .customerId(
                                scoreCard.getCustomerId()
                        )
                        .cibilScore(
                                scoreCard.getCibilScore()
                        )
                        .cibilStatus(
                                cibilStatus
                        )
                        .riskCategory(
                                scoreCard.getRiskCategory()
                        )
                        .eligibleLoanAmount(
                                scoreCard.getEligibleLoanAmount()
                        )
                        .approvalType(
                                approvalType
                        )
                        .currentStatus(
                                scoreCard.getCurrentStatus()
                        )
                        .rejectionReason(
                                scoreCard.getRejectionReason()
                        )
                        .appliedDate(
                                scoreCard.getAppliedDate()
                        )
                        .build();


        return ResponseApi
                .<ScoreCardResponse>builder()
                .success(true)
                .message(
                        "Scorecard fetched successfully"
                )
                .data(response)
                .build();
    }


    private String getCibilStatus(
            Integer cibilScore
    ) {

        if (cibilScore == null) {
            return "UNKNOWN";
        }

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


    private String getApprovalType(
            Integer cibilScore
    ) {

        if (cibilScore == null || cibilScore < 650) {
            return "REJECTED";
        }

        if (cibilScore > 800) {
            return "AUTO_APPROVAL";
        }

        return "MANUAL_REVIEW";
    }
}