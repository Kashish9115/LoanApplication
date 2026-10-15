
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
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreCardServiceImpl implements ScoreCardService {

    private final EligibilityResultRepository eligibilityResultRepository;
    private final ScoreCardRepository scoreCardRepository;

    @Override
    @Transactional
    public ResponseApi<ScoreCardResponse> generateScoreCard(
            Integer customerId
    ) {

        log.info(
                "Starting scorecard generation for customerId: {}",
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

        Integer cibilScore = eligibilityResult.getCibilScore();

        boolean eligible =
                Boolean.TRUE.equals(eligibilityResult.getIsEligible());

        String cibilStatus = getCibilStatus(cibilScore);
        String riskCategory = cibilStatus;

        String approvalType;
        String currentStatus;

        String rejectionReason =
                eligibilityResult.getRejectionReason();

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

        log.info(
                "Scorecard decision determined for customerId: {}, status: {}, approvalType: {}",
                customerId,
                currentStatus,
                approvalType
        );

        ScoreCard scoreCard =
                ScoreCard.builder()
                        .customerId(eligibilityResult.getCustomerId())
                        .currentStatus(currentStatus)
                        .rejectionReason(rejectionReason)
                        .appliedDate(LocalDateTime.now())
                        .cibilScore(cibilScore)
                        .riskCategory(riskCategory)
                        .eligibleLoanAmount(
                                eligibilityResult.getLoanAmount()
                        )
                        .build();

        ScoreCard savedScoreCard =
                scoreCardRepository.save(scoreCard);

        log.info(
                "Scorecard saved successfully. scoreCardId: {}, customerId: {}",
                savedScoreCard.getScoreCardId(),
                customerId
        );

        ScoreCardResponse response =
                ScoreCardResponse.builder()
                        .scoreCardId(savedScoreCard.getScoreCardId())
                        .customerId(savedScoreCard.getCustomerId())
                        .cibilScore(savedScoreCard.getCibilScore())
                        .cibilStatus(cibilStatus)
                        .riskCategory(savedScoreCard.getRiskCategory())
                        .eligibleLoanAmount(
                                savedScoreCard.getEligibleLoanAmount()
                        )
                        .approvalType(approvalType)
                        .currentStatus(savedScoreCard.getCurrentStatus())
                        .rejectionReason(savedScoreCard.getRejectionReason())
                        .appliedDate(savedScoreCard.getAppliedDate())
                        .build();

        return ResponseApi.<ScoreCardResponse>builder()
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

        log.info(
                "Fetching latest scorecard for customerId: {}",
                customerId
        );

        ScoreCard scoreCard =
                scoreCardRepository
                        .findTopByCustomerIdOrderByScoreCardIdDesc(customerId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Scorecard not found for customerId: {}",
                                    customerId
                            );

                            return new ResourceNotFoundException(
                                    "Scorecard not found for customer: "
                                            + customerId
                            );
                        });

        String cibilStatus =
                getCibilStatus(scoreCard.getCibilScore());

        String approvalType =
                getApprovalType(scoreCard.getCibilScore());

        ScoreCardResponse response =
                ScoreCardResponse.builder()
                        .scoreCardId(scoreCard.getScoreCardId())
                        .customerId(scoreCard.getCustomerId())
                        .cibilScore(scoreCard.getCibilScore())
                        .cibilStatus(cibilStatus)
                        .riskCategory(scoreCard.getRiskCategory())
                        .eligibleLoanAmount(scoreCard.getEligibleLoanAmount())
                        .approvalType(approvalType)
                        .currentStatus(scoreCard.getCurrentStatus())
                        .rejectionReason(scoreCard.getRejectionReason())
                        .appliedDate(scoreCard.getAppliedDate())
                        .build();

        log.info(
                "Latest scorecard fetched successfully. scoreCardId: {}, customerId: {}",
                scoreCard.getScoreCardId(),
                customerId
        );

        return ResponseApi.<ScoreCardResponse>builder()
                .success(true)
                .message("Scorecard fetched successfully")
                .data(response)
                .build();
    }

    private String getCibilStatus(Integer cibilScore) {

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

    private String getApprovalType(Integer cibilScore) {

        if (cibilScore == null || cibilScore < 650) {
            return "REJECTED";
        }

        if (cibilScore > 800) {
            return "AUTO_APPROVAL";
        }

        return "MANUAL_REVIEW";
    }
}
