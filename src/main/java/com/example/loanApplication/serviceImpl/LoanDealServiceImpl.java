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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanDealServiceImpl implements LoanDealService {

    private final LoanDealRepository loanDealRepository;
    private final EligibilityResultRepository eligibilityResultRepository;
    private final ScoreCardRepository scoreCardRepository;


    @Override
    @Transactional
    public ResponseApi<LoanDealResponse> createLoanDeal(
            LoanDealRequest request
    ) {

        EligibilityResult eligibilityResult =
                eligibilityResultRepository
                        .findTopByCustomerIdOrderByEligibilityIdDesc(
                                request.getCustomerId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Eligibility result not found for customer: "
                                                + request.getCustomerId()
                                )
                        );


        if (!Boolean.TRUE.equals(
                eligibilityResult.getIsEligible()
        )) {

            throw new InvalidLoanDealException(
                    "Customer is not eligible for a loan."
            );
        }


        ScoreCard scoreCard =
                scoreCardRepository
                        .findTopByCustomerIdOrderByScoreCardIdDesc(
                                request.getCustomerId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Scorecard not found for customer: "
                                                + request.getCustomerId()
                                )
                        );


        validateEmiDay(
                request.getEmiDay()
        );


        BigDecimal eligibleAmount =
                eligibilityResult.getLoanAmount();


        if (request.getRequestedLoanAmount()
                .compareTo(eligibleAmount) > 0) {

            throw new InvalidLoanDealException(
                    "Requested loan amount cannot exceed eligible loan amount of "
                            + eligibleAmount
            );
        }


        Integer cibilScore =
                scoreCard.getCibilScore();


        LoanDealStatus dealStatus;

        BigDecimal approvedAmount = null;


        if (cibilScore > 800) {

            dealStatus =
                    LoanDealStatus.AUTO_APPROVED;

            approvedAmount =
                    request.getRequestedLoanAmount();

        } else if (cibilScore >= 650) {

            dealStatus =
                    LoanDealStatus.MANUAL_REVIEW;

        } else {

            throw new InvalidLoanDealException(
                    "Customer is not eligible to create loan deal."
            );
        }


        LoanDeal loanDeal =
                LoanDeal.builder()
                        .customerId(
                                request.getCustomerId()
                        )
                        .loanType(
                                request.getLoanType()
                        )
                        .loanAmount(
                                request.getRequestedLoanAmount()
                        )
                        .interestRate(
                                null
                        )
                        .tenureMonths(
                                request.getTenureMonths()
                        )
                        .emiAmount(
                                null
                        )
                        .bankName(
                                request.getBankName()
                        )
                        .bankAccountNumber(
                                request.getBankAccountNumber()
                        )
                        .accountHolderName(
                                request.getAccountHolderName()
                        )
                        .ifscCode(
                                request.getIfscCode()
                        )
                        .emiDay(
                                request.getEmiDay()
                        )
                        .preferredDisbursementDate(
                                request.getPreferredDisbursementDate()
                        )
                        .approvedAmount(
                                approvedAmount
                        )
                        .dealStatus(
                                dealStatus
                        )
                        .build();


        LoanDeal savedLoanDeal =
                loanDealRepository.save(
                        loanDeal
                );


        LoanDealResponse response =
                buildLoanDealResponse(
                        savedLoanDeal,
                        eligibilityResult,
                        scoreCard
                );


        return ResponseApi
                .<LoanDealResponse>builder()
                .success(true)
                .message(
                        "Loan deal created successfully"
                )
                .data(response)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseApi<LoanDealResponse> getLoanDealById(
            Integer dealId
    ) {

        LoanDeal loanDeal =
                loanDealRepository
                        .findById(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with ID: "
                                                + dealId
                                )
                        );


        EligibilityResult eligibilityResult =
                eligibilityResultRepository
                        .findTopByCustomerIdOrderByEligibilityIdDesc(
                                loanDeal.getCustomerId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Eligibility result not found for customer: "
                                                + loanDeal.getCustomerId()
                                )
                        );


        ScoreCard scoreCard =
                scoreCardRepository
                        .findTopByCustomerIdOrderByScoreCardIdDesc(
                                loanDeal.getCustomerId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Scorecard not found for customer: "
                                                + loanDeal.getCustomerId()
                                )
                        );


        LoanDealResponse response =
                buildLoanDealResponse(
                        loanDeal,
                        eligibilityResult,
                        scoreCard
                );


        return ResponseApi
                .<LoanDealResponse>builder()
                .success(true)
                .message(
                        "Loan deal fetched successfully"
                )
                .data(response)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseApi<LoanDealResponse>
    getLatestLoanDealByCustomer(
            Integer customerId
    ) {

        LoanDeal loanDeal =
                loanDealRepository
                        .findTopByCustomerIdOrderByDealIdDesc(
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found for customer: "
                                                + customerId
                                )
                        );


        EligibilityResult eligibilityResult =
                eligibilityResultRepository
                        .findTopByCustomerIdOrderByEligibilityIdDesc(
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Eligibility result not found for customer: "
                                                + customerId
                                )
                        );


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


        LoanDealResponse response =
                buildLoanDealResponse(
                        loanDeal,
                        eligibilityResult,
                        scoreCard
                );


        return ResponseApi
                .<LoanDealResponse>builder()
                .success(true)
                .message(
                        "Latest loan deal fetched successfully"
                )
                .data(response)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PageResponse<LoanDealResponse>>
    getLoanDealsByCustomer(
            Integer customerId,
            int page,
            int size
    ) {

        /*
         * STEP 1
         * Validate pagination values.
         */
        if (page < 0) {

            throw new InvalidLoanDealException(
                    "Page number cannot be negative."
            );
        }


        if (size <= 0) {

            throw new InvalidLoanDealException(
                    "Page size must be greater than zero."
            );
        }


        /*
         * Optional protection so someone
         * does not request thousands of records.
         */
        if (size > 100) {

            throw new InvalidLoanDealException(
                    "Page size cannot be greater than 100."
            );
        }


        /*
         * STEP 2
         * Create Pageable.
         */
        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );


        /*
         * STEP 3
         * Fetch paginated LoanDeals.
         */
        Page<LoanDeal> loanDealPage =
                loanDealRepository
                        .findByCustomerIdOrderByDealIdDesc(
                                customerId,
                                pageable
                        );


        if (loanDealPage.isEmpty()) {

            throw new ResourceNotFoundException(
                    "No loan deals found for customer: "
                            + customerId
            );
        }


        /*
         * STEP 4
         * Fetch latest eligibility and scorecard.
         */
        EligibilityResult eligibilityResult =
                eligibilityResultRepository
                        .findTopByCustomerIdOrderByEligibilityIdDesc(
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Eligibility result not found for customer: "
                                                + customerId
                                )
                        );


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
         * STEP 5
         * Convert Page<LoanDeal>
         * into List<LoanDealResponse>.
         */
        List<LoanDealResponse> content =
                loanDealPage
                        .getContent()
                        .stream()
                        .map(loanDeal ->
                                buildLoanDealResponse(
                                        loanDeal,
                                        eligibilityResult,
                                        scoreCard
                                )
                        )
                        .toList();


        /*
         * STEP 6
         * Build our custom PageResponse.
         */
        PageResponse<LoanDealResponse> pageResponse =
                PageResponse
                        .<LoanDealResponse>builder()
                        .content(
                                content
                        )
                        .pageNumber(
                                loanDealPage.getNumber()
                        )
                        .pageSize(
                                loanDealPage.getSize()
                        )
                        .totalElements(
                                loanDealPage.getTotalElements()
                        )
                        .totalPages(
                                loanDealPage.getTotalPages()
                        )
                        .first(
                                loanDealPage.isFirst()
                        )
                        .last(
                                loanDealPage.isLast()
                        )
                        .build();


        return ResponseApi
                .<PageResponse<LoanDealResponse>>builder()
                .success(true)
                .message(
                        "Loan deals fetched successfully"
                )
                .data(
                        pageResponse
                )
                .build();
    }


    private void validateEmiDay(
            Integer emiDay
    ) {

        if (emiDay == null) {

            throw new InvalidLoanDealException(
                    "EMI day is required."
            );
        }


        if (emiDay != 5
                && emiDay != 7
                && emiDay != 10
                && emiDay != 15) {

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
                .dealId(
                        loanDeal.getDealId()
                )
                .customerId(
                        loanDeal.getCustomerId()
                )
                .loanType(
                        loanDeal.getLoanType()
                )
                .requestedLoanAmount(
                        loanDeal.getLoanAmount()
                )
                .eligibleLoanAmount(
                        eligibilityResult.getLoanAmount()
                )
                .approvedAmount(
                        loanDeal.getApprovedAmount()
                )
                .tenureMonths(
                        loanDeal.getTenureMonths()
                )
                .emiDay(
                        loanDeal.getEmiDay()
                )
                .bankName(
                        loanDeal.getBankName()
                )
                .bankAccountNumber(
                        loanDeal.getBankAccountNumber()
                )
                .accountHolderName(
                        loanDeal.getAccountHolderName()
                )
                .ifscCode(
                        loanDeal.getIfscCode()
                )
                .preferredDisbursementDate(
                        loanDeal.getPreferredDisbursementDate()
                )
                .cibilScore(
                        scoreCard.getCibilScore()
                )
                .cibilStatus(
                        getCibilStatus(
                                scoreCard.getCibilScore()
                        )
                )
                .dealStatus(
                        loanDeal.getDealStatus()
                )
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
}