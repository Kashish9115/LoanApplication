
package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.LoanDealRequest;
import com.example.loanApplication.dto.LoanDealResponse;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.entity.EligibilityResult;
import com.example.loanApplication.entity.LoanDeal;
import com.example.loanApplication.entity.ScoreCard;
import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.exception.InvalidLoanDealException;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.EligibilityResultRepository;
import com.example.loanApplication.repository.LoanDealRepository;
import com.example.loanApplication.repository.ScoreCardRepository;
import com.example.loanApplication.service.LoanDealService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanDealServiceImpl implements LoanDealService {

    private final LoanDealRepository loanDealRepository;
    private final EligibilityResultRepository eligibilityResultRepository;
    private final ScoreCardRepository scoreCardRepository;

    @Override
    @Transactional
    public ResponseApi<LoanDealResponse> createLoanDeal(
            LoanDealRequest request
    ) {

        log.info("Starting loan deal creation for customerId: {}",
                request.getCustomerId());

        EligibilityResult eligibilityResult =
                eligibilityResultRepository
                        .findTopByCustomerIdOrderByEligibilityIdDesc(
                                request.getCustomerId()
                        )
                        .orElseThrow(() -> {
                            log.warn(
                                    "Eligibility result not found for customerId: {}",
                                    request.getCustomerId()
                            );
                            return new ResourceNotFoundException(
                                    "Eligibility result not found for customer: "
                                            + request.getCustomerId()
                            );
                        });

        log.info(
                "Eligibility verified for customerId: {}, eligible: {}, eligibleAmount: {}",
                request.getCustomerId(),
                eligibilityResult.getIsEligible(),
                eligibilityResult.getLoanAmount()
        );

        if (!Boolean.TRUE.equals(eligibilityResult.getIsEligible())) {
            log.warn(
                    "Loan deal creation rejected because customer is not eligible. customerId: {}",
                    request.getCustomerId()
            );

            throw new InvalidLoanDealException(
                    "Customer is not eligible for a loan."
            );
        }

        ScoreCard scoreCard =
                scoreCardRepository
                        .findTopByCustomerIdOrderByScoreCardIdDesc(
                                request.getCustomerId()
                        )
                        .orElseThrow(() -> {
                            log.warn(
                                    "Scorecard not found for customerId: {}",
                                    request.getCustomerId()
                            );
                            return new ResourceNotFoundException(
                                    "Scorecard not found for customer: "
                                            + request.getCustomerId()
                            );
                        });

        log.info(
                "Scorecard fetched for customerId: {}, CIBIL score: {}",
                request.getCustomerId(),
                scoreCard.getCibilScore()
        );

        validateEmiDay(request.getEmiDay());

        BigDecimal eligibleAmount = eligibilityResult.getLoanAmount();

        if (request.getRequestedLoanAmount()
                .compareTo(eligibleAmount) > 0) {

            log.warn(
                    "Requested loan amount {} exceeds eligible amount {} for customerId: {}",
                    request.getRequestedLoanAmount(),
                    eligibleAmount,
                    request.getCustomerId()
            );

            throw new InvalidLoanDealException(
                    "Requested loan amount cannot exceed eligible loan amount of "
                            + eligibleAmount
            );
        }

        Integer cibilScore = scoreCard.getCibilScore();

        LoanDealStatus dealStatus;
        BigDecimal approvedAmount = null;

        if (cibilScore == null) {
            log.warn(
                    "Loan deal creation rejected because CIBIL score is unavailable. customerId: {}",
                    request.getCustomerId()
            );

            throw new InvalidLoanDealException(
                    "CIBIL score is not available for this customer."
            );
        }

        if (cibilScore > 800) {
            dealStatus = LoanDealStatus.AUTO_APPROVED;
            approvedAmount = request.getRequestedLoanAmount();

        } else if (cibilScore >= 650) {
            dealStatus = LoanDealStatus.MANUAL_REVIEW;

        } else {
            log.warn(
                    "Loan deal creation rejected due to low CIBIL score. customerId: {}, score: {}",
                    request.getCustomerId(),
                    cibilScore
            );

            throw new InvalidLoanDealException(
                    "Customer is not eligible to create loan deal."
            );
        }

        log.info(
                "Loan deal decision determined. customerId: {}, CIBIL score: {}, status: {}",
                request.getCustomerId(),
                cibilScore,
                dealStatus
        );

        LoanDeal loanDeal =
                LoanDeal.builder()
                        .customerId(request.getCustomerId())
                        .loanType(request.getLoanType())
                        .loanAmount(request.getRequestedLoanAmount())
                        .interestRate(null)
                        .tenureMonths(request.getTenureMonths())
                        .emiAmount(null)
                        .bankName(request.getBankName())
                        .bankAccountNumber(request.getBankAccountNumber())
                        .accountHolderName(request.getAccountHolderName())
                        .ifscCode(request.getIfscCode())
                        .emiDay(request.getEmiDay())
                        .approvedAmount(approvedAmount)
                        .dealStatus(dealStatus)
                        .build();

        LoanDeal savedLoanDeal = loanDealRepository.save(loanDeal);

        log.info(
                "Loan deal created successfully. dealId: {}, customerId: {}, status: {}",
                savedLoanDeal.getDealId(),
                savedLoanDeal.getCustomerId(),
                savedLoanDeal.getDealStatus()
        );

        LoanDealResponse response =
                buildLoanDealResponse(
                        savedLoanDeal,
                        eligibilityResult,
                        scoreCard
                );

        return ResponseApi.<LoanDealResponse>builder()
                .success(true)
                .message("Loan deal created successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<LoanDealResponse> getLoanDealById(
            Integer dealId
    ) {

        log.info("Fetching loan deal by dealId: {}", dealId);

        LoanDeal loanDeal =
                loanDealRepository.findById(dealId)
                        .orElseThrow(() -> {
                            log.warn("Loan deal not found. dealId: {}", dealId);
                            return new ResourceNotFoundException(
                                    "Loan deal not found with ID: " + dealId
                            );
                        });

        EligibilityResult eligibilityResult =
                eligibilityResultRepository
                        .findTopByCustomerIdOrderByEligibilityIdDesc(
                                loanDeal.getCustomerId()
                        )
                        .orElseThrow(() -> {
                            log.warn(
                                    "Eligibility result not found for customerId: {}",
                                    loanDeal.getCustomerId()
                            );
                            return new ResourceNotFoundException(
                                    "Eligibility result not found for customer: "
                                            + loanDeal.getCustomerId()
                            );
                        });

        ScoreCard scoreCard =
                scoreCardRepository
                        .findTopByCustomerIdOrderByScoreCardIdDesc(
                                loanDeal.getCustomerId()
                        )
                        .orElseThrow(() -> {
                            log.warn(
                                    "Scorecard not found for customerId: {}",
                                    loanDeal.getCustomerId()
                            );
                            return new ResourceNotFoundException(
                                    "Scorecard not found for customer: "
                                            + loanDeal.getCustomerId()
                            );
                        });

        LoanDealResponse response =
                buildLoanDealResponse(
                        loanDeal,
                        eligibilityResult,
                        scoreCard
                );

        log.info(
                "Loan deal fetched successfully. dealId: {}, customerId: {}",
                loanDeal.getDealId(),
                loanDeal.getCustomerId()
        );

        return ResponseApi.<LoanDealResponse>builder()
                .success(true)
                .message("Loan deal fetched successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<LoanDealResponse> getLatestLoanDealByCustomer(
            Integer customerId
    ) {

        log.info("Fetching latest loan deal for customerId: {}", customerId);

        LoanDeal loanDeal =
                loanDealRepository
                        .findTopByCustomerIdOrderByDealIdDesc(customerId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Loan deal not found for customerId: {}",
                                    customerId
                            );
                            return new ResourceNotFoundException(
                                    "Loan deal not found for customer: "
                                            + customerId
                            );
                        });

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

        LoanDealResponse response =
                buildLoanDealResponse(
                        loanDeal,
                        eligibilityResult,
                        scoreCard
                );

        log.info(
                "Latest loan deal fetched successfully. dealId: {}, customerId: {}",
                loanDeal.getDealId(),
                customerId
        );

        return ResponseApi.<LoanDealResponse>builder()
                .success(true)
                .message("Latest loan deal fetched successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PageResponse<LoanDealResponse>> getLoanDealsByCustomer(
            Integer customerId,
            int page,
            int size
    ) {

        log.info(
                "Fetching loan deals for customerId: {}, page: {}, size: {}",
                customerId,
                page,
                size
        );

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<LoanDeal> loanDealPage =
                loanDealRepository
                        .findByCustomerIdOrderByDealIdDesc(
                                customerId,
                                pageable
                        );

        log.info(
                "Loan deals fetched for customerId: {}, totalElements: {}, page: {}",
                customerId,
                loanDealPage.getTotalElements(),
                page
        );

        if (loanDealPage.isEmpty()) {
            log.warn("No loan deals found for customerId: {}", customerId);

            throw new ResourceNotFoundException(
                    "No loan deals found for customer: " + customerId
            );
        }

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

        List<LoanDealResponse> content =
                loanDealPage.getContent()
                        .stream()
                        .map(loanDeal ->
                                buildLoanDealResponse(
                                        loanDeal,
                                        eligibilityResult,
                                        scoreCard
                                )
                        )
                        .toList();

        PageResponse<LoanDealResponse> pageResponse =
                PageResponse.<LoanDealResponse>builder()
                        .content(content)
                        .pageNumber(loanDealPage.getNumber())
                        .pageSize(loanDealPage.getSize())
                        .totalElements(loanDealPage.getTotalElements())
                        .totalPages(loanDealPage.getTotalPages())
                        .first(loanDealPage.isFirst())
                        .last(loanDealPage.isLast())
                        .build();

        log.info(
                "Loan deals response prepared for customerId: {}, returnedRecords: {}, totalElements: {}",
                customerId,
                content.size(),
                loanDealPage.getTotalElements()
        );

        return ResponseApi.<PageResponse<LoanDealResponse>>builder()
                .success(true)
                .message("Loan deals fetched successfully")
                .data(pageResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PageResponse<LoanDealResponse>> getManualReviewDeals(
            int page,
            int size
    ) {

        log.info(
                "Fetching manual review deals. page: {}, size: {}",
                page,
                size
        );

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<LoanDeal> loanDealPage =
                loanDealRepository.findByDealStatusOrderByDealIdDesc(
                        LoanDealStatus.MANUAL_REVIEW,
                        pageable
                );

        log.info(
                "Manual review deals fetched. totalElements: {}, page: {}",
                loanDealPage.getTotalElements(),
                page
        );

        List<LoanDealResponse> content =
                loanDealPage.getContent()
                        .stream()
                        .map(loanDeal -> {

                            EligibilityResult eligibilityResult =
                                    eligibilityResultRepository
                                            .findTopByCustomerIdOrderByEligibilityIdDesc(
                                                    loanDeal.getCustomerId()
                                            )
                                            .orElseThrow(() -> {
                                                log.warn(
                                                        "Eligibility result not found for customerId: {}",
                                                        loanDeal.getCustomerId()
                                                );
                                                return new ResourceNotFoundException(
                                                        "Eligibility result not found for customer: "
                                                                + loanDeal.getCustomerId()
                                                );
                                            });

                            ScoreCard scoreCard =
                                    scoreCardRepository
                                            .findTopByCustomerIdOrderByScoreCardIdDesc(
                                                    loanDeal.getCustomerId()
                                            )
                                            .orElseThrow(() -> {
                                                log.warn(
                                                        "Scorecard not found for customerId: {}",
                                                        loanDeal.getCustomerId()
                                                );
                                                return new ResourceNotFoundException(
                                                        "Scorecard not found for customer: "
                                                                + loanDeal.getCustomerId()
                                                );
                                            });

                            return buildLoanDealResponse(
                                    loanDeal,
                                    eligibilityResult,
                                    scoreCard
                            );
                        })
                        .toList();

        PageResponse<LoanDealResponse> pageResponse =
                PageResponse.<LoanDealResponse>builder()
                        .content(content)
                        .pageNumber(loanDealPage.getNumber())
                        .pageSize(loanDealPage.getSize())
                        .totalElements(loanDealPage.getTotalElements())
                        .totalPages(loanDealPage.getTotalPages())
                        .first(loanDealPage.isFirst())
                        .last(loanDealPage.isLast())
                        .build();

        log.info(
                "Manual review response prepared. returnedRecords: {}, totalElements: {}",
                content.size(),
                loanDealPage.getTotalElements()
        );

        return ResponseApi.<PageResponse<LoanDealResponse>>builder()
                .success(true)
                .message("Manual review deals fetched successfully")
                .data(pageResponse)
                .build();
    }

    private void validatePagination(int page, int size) {

        if (page < 0) {
            log.warn(
                    "Invalid pagination requested. page: {}, size: {}",
                    page,
                    size
            );

            throw new InvalidLoanDealException(
                    "Page number cannot be negative."
            );
        }

        if (size <= 0 || size > 100) {
            log.warn(
                    "Invalid pagination requested. page: {}, size: {}",
                    page,
                    size
            );

            throw new InvalidLoanDealException(
                    "Page size must be between 1 and 100."
            );
        }
    }

    private void validateEmiDay(Integer emiDay) {

        if (emiDay == null) {
            log.warn("EMI day is missing.");

            throw new InvalidLoanDealException(
                    "EMI day is required."
            );
        }

        if (emiDay != 5 && emiDay != 7
                && emiDay != 10 && emiDay != 15) {

            log.warn("Invalid EMI day requested: {}", emiDay);

            throw new InvalidLoanDealException(
                    "EMI day must be one of: 5, 7, 10, 15."
            );
        }
    }

    private LoanDealResponse buildLoanDealResponse(
            LoanDeal loanDeal,
            EligibilityResult eligibilityResult,
            ScoreCard scoreCard
    ) {

        return LoanDealResponse.builder()
                .dealId(loanDeal.getDealId())
                .customerId(loanDeal.getCustomerId())
                .loanType(loanDeal.getLoanType())
                .requestedLoanAmount(loanDeal.getLoanAmount())
                .eligibleLoanAmount(eligibilityResult.getLoanAmount())
                .approvedAmount(loanDeal.getApprovedAmount())
                .tenureMonths(loanDeal.getTenureMonths())
                .emiDay(loanDeal.getEmiDay())
                .bankName(loanDeal.getBankName())
                .bankAccountNumber(loanDeal.getBankAccountNumber())
                .accountHolderName(loanDeal.getAccountHolderName())
                .ifscCode(loanDeal.getIfscCode())
                .preferredDisbursementDate(
                        loanDeal.getPreferredDisbursementDate()
                )
                .cibilScore(scoreCard.getCibilScore())
                .cibilStatus(getCibilStatus(scoreCard.getCibilScore()))
                .dealStatus(loanDeal.getDealStatus())
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
}
