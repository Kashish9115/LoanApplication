package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.SanctionResponse;
import com.example.loanApplication.entity.LoanDeal;
import com.example.loanApplication.entity.SanctionLetter;
import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.enumeration.LoanType;
import com.example.loanApplication.exception.InvalidLoanDealException;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.LoanDealRepository;
import com.example.loanApplication.repository.SanctionLetterRepository;
import com.example.loanApplication.service.SanctionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SanctionServiceImpl implements SanctionService {

    private final LoanDealRepository loanDealRepository;
    private final SanctionLetterRepository sanctionLetterRepository;


    @Override
    @Transactional
    public ResponseApi<SanctionResponse> sanctionLoan(
            Integer dealId
    ) {

        /*
         * STEP 1
         * Find Loan Deal.
         */
        LoanDeal loanDeal =
                loanDealRepository
                        .findById(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with ID: "
                                                + dealId
                                )
                        );


        /*
         * STEP 2
         * Only approved deals can be sanctioned.
         *
         * AUTO_APPROVED -> direct approval
         * APPROVED      -> officer approved
         */
        if (loanDeal.getDealStatus()
                != LoanDealStatus.AUTO_APPROVED
                &&
                loanDeal.getDealStatus()
                        != LoanDealStatus.APPROVED) {

            throw new InvalidLoanDealException(
                    "Only APPROVED or AUTO_APPROVED loan deals can be sanctioned."
            );
        }



        /*
         * STEP 3
         * Approved amount must exist.
         */
        if (loanDeal.getApprovedAmount() == null
                ||
                loanDeal.getApprovedAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidLoanDealException(
                    "Approved loan amount is not available for this deal."
            );
        }


        /*
         * STEP 4
         * Tenure must be valid.
         */
        if (loanDeal.getTenureMonths() == null
                ||
                loanDeal.getTenureMonths() <= 0) {

            throw new InvalidLoanDealException(
                    "Valid loan tenure is required before sanction."
            );
        }


        /*
         * STEP 5
         * Determine interest rate according to loan type.
         *
         * HOME_LOAN    = 15%
         * VEHICLE_LOAN = 10%
         */
        BigDecimal interestRate =
                getInterestRate(
                        loanDeal.getLoanType()
                );


        /*
         * STEP 6
         * Calculate EMI.
         */
        BigDecimal emiAmount =
                calculateEmi(
                        loanDeal.getApprovedAmount(),
                        interestRate,
                        loanDeal.getTenureMonths()
                );


        /*
         * STEP 7
         * Create sanction letter.
         */
        SanctionLetter sanctionLetter =
                SanctionLetter.builder()
                        .dealId(
                                loanDeal.getDealId()
                        )
                        .loanAmount(
                                loanDeal.getApprovedAmount()
                        )
                        .interestRate(
                                interestRate
                        )
                        .tenureMonths(
                                loanDeal.getTenureMonths()
                        )
                        .emiAmount(
                                emiAmount
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();


        SanctionLetter savedSanctionLetter =
                sanctionLetterRepository.save(
                        sanctionLetter
                );


        /*
         * STEP 8
         * Update LoanDeal.
         */
        loanDeal.setInterestRate(
                interestRate
        );

        loanDeal.setEmiAmount(
                emiAmount
        );

        loanDeal.setDealStatus(
                LoanDealStatus.SANCTIONED
        );


        LoanDeal updatedLoanDeal =
                loanDealRepository.save(
                        loanDeal
                );


        /*
         * STEP 9
         * Prepare response.
         */
        SanctionResponse response =
                buildSanctionResponse(
                        savedSanctionLetter,
                        updatedLoanDeal,
                        "Loan sanctioned successfully"
                );


        return ResponseApi
                .<SanctionResponse>builder()
                .success(true)
                .message(
                        "Loan sanctioned successfully"
                )
                .data(response)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseApi<SanctionResponse> getSanctionByDealId(
            Integer dealId
    ) {

        /*
         * First verify loan deal exists.
         */
        LoanDeal loanDeal =
                loanDealRepository
                        .findById(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with ID: "
                                                + dealId
                                )
                        );


        /*
         * Get latest sanction letter for deal.
         */
        SanctionLetter sanctionLetter =
                sanctionLetterRepository
                        .findTopByDealIdOrderBySanctionIdDesc(
                                dealId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sanction letter not found for deal: "
                                                + dealId
                                )
                        );


        SanctionResponse response =
                buildSanctionResponse(
                        sanctionLetter,
                        loanDeal,
                        "Sanction details fetched successfully"
                );


        return ResponseApi
                .<SanctionResponse>builder()
                .success(true)
                .message(
                        "Sanction details fetched successfully"
                )
                .data(response)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseApi<SanctionResponse> getSanctionById(
            Integer sanctionId
    ) {

        /*
         * Find sanction letter.
         */
        SanctionLetter sanctionLetter =
                sanctionLetterRepository
                        .findById(sanctionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sanction letter not found with ID: "
                                                + sanctionId
                                )
                        );


        /*
         * Find associated loan deal.
         */
        LoanDeal loanDeal =
                loanDealRepository
                        .findById(
                                sanctionLetter.getDealId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with ID: "
                                                + sanctionLetter.getDealId()
                                )
                        );


        SanctionResponse response =
                buildSanctionResponse(
                        sanctionLetter,
                        loanDeal,
                        "Sanction details fetched successfully"
                );


        return ResponseApi
                .<SanctionResponse>builder()
                .success(true)
                .message(
                        "Sanction details fetched successfully"
                )
                .data(response)
                .build();
    }


    /*
     * Determine annual interest rate.
     */
    private BigDecimal getInterestRate(
            LoanType loanType
    ) {

        if (loanType == null) {

            throw new InvalidLoanDealException(
                    "Loan type is required for sanction."
            );
        }


        if (loanType == LoanType.HOME_LOAN) {

            return new BigDecimal("15.0000");
        }


        if (loanType == LoanType.VEHICLE_LOAN) {

            return new BigDecimal("10.0000");
        }


        throw new InvalidLoanDealException(
                "Unsupported loan type: " + loanType
        );
    }


    /*
     * EMI Formula:
     *
     * EMI =
     * P × R × (1 + R)^N
     * -----------------
     *    (1 + R)^N - 1
     *
     * P = Principal / Approved Amount
     * R = Monthly interest rate
     * N = Number of months
     */
    private BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal annualInterestRate,
            Integer tenureMonths
    ) {

        double principalValue =
                principal.doubleValue();

        double annualRate =
                annualInterestRate.doubleValue();


        /*
         * Convert annual percentage into
         * monthly decimal rate.
         *
         * Example:
         *
         * 15%
         * 15 / 12 / 100
         * = 0.0125
         */
        double monthlyRate =
                annualRate / 12 / 100;


        /*
         * (1 + R)^N
         */
        double power =
                Math.pow(
                        1 + monthlyRate,
                        tenureMonths
                );


        /*
         * EMI calculation.
         */
        double emi =
                principalValue
                        * monthlyRate
                        * power
                        /
                        (power - 1);


        return BigDecimal
                .valueOf(emi)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }


    /*
     * Convert entity data into DTO response.
     */
    private SanctionResponse buildSanctionResponse(
            SanctionLetter sanctionLetter,
            LoanDeal loanDeal,
            String message
    ) {

        return SanctionResponse.builder()
                .sanctionId(
                        sanctionLetter.getSanctionId()
                )
                .dealId(
                        loanDeal.getDealId()
                )
                .customerId(
                        loanDeal.getCustomerId()
                )
                .loanType(
                        loanDeal.getLoanType()
                )
                .approvedLoanAmount(
                        sanctionLetter.getLoanAmount()
                )
                .interestRate(
                        sanctionLetter.getInterestRate()
                )
                .tenureMonths(
                        sanctionLetter.getTenureMonths()
                )
                .emiAmount(
                        sanctionLetter.getEmiAmount()
                )
                .emiDay(
                        loanDeal.getEmiDay()
                )
                .dealStatus(
                        loanDeal.getDealStatus()
                )
                .createdAt(
                        sanctionLetter.getCreatedAt()
                )
                .message(
                        message
                )
                .build();
    }
}