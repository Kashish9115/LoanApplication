

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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanctionServiceImpl implements SanctionService {

    private final LoanDealRepository loanDealRepository;
    private final SanctionLetterRepository sanctionLetterRepository;

    /*
     * Generate a new sanction letter.
     */
    @Override
    @Transactional
    public ResponseApi<SanctionResponse> sanctionLoan(Integer dealId) {

        LoanDeal loanDeal = loanDealRepository
                .findById(dealId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan deal not found with ID: " + dealId
                        )
                );

        // Only approved deals can be sanctioned.
        if (loanDeal.getDealStatus() != LoanDealStatus.AUTO_APPROVED
                && loanDeal.getDealStatus() != LoanDealStatus.APPROVED) {

            throw new InvalidLoanDealException(
                    "Only APPROVED or AUTO_APPROVED loan deals can be sanctioned."
            );
        }

        // Validate approved amount.
        if (loanDeal.getApprovedAmount() == null
                || loanDeal.getApprovedAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidLoanDealException(
                    "Approved loan amount is not available for this deal."
            );
        }

        // Validate tenure.
        if (loanDeal.getTenureMonths() == null
                || loanDeal.getTenureMonths() <= 0) {

            throw new InvalidLoanDealException(
                    "Valid loan tenure is required before sanction."
            );
        }

        // Calculate interest rate according to loan type.
        BigDecimal interestRate =
                getInterestRate(loanDeal.getLoanType());

        // Calculate monthly EMI.
        BigDecimal emiAmount = calculateEmi(
                loanDeal.getApprovedAmount(),
                interestRate,
                loanDeal.getTenureMonths()
        );

        // Create sanction letter.
        SanctionLetter sanctionLetter = SanctionLetter.builder()
                .dealId(loanDeal.getDealId())
                .loanAmount(loanDeal.getApprovedAmount())
                .interestRate(interestRate)
                .tenureMonths(loanDeal.getTenureMonths())
                .emiAmount(emiAmount)
                .createdAt(LocalDateTime.now())
                .build();

        SanctionLetter savedSanctionLetter =
                sanctionLetterRepository.save(sanctionLetter);

        // Update the existing loan deal.
        loanDeal.setInterestRate(interestRate);
        loanDeal.setEmiAmount(emiAmount);
        loanDeal.setDealStatus(LoanDealStatus.SANCTIONED);

        LoanDeal updatedLoanDeal =
                loanDealRepository.save(loanDeal);

        SanctionResponse response = buildSanctionResponse(
                savedSanctionLetter,
                updatedLoanDeal,
                "Loan sanctioned successfully"
        );

        return ResponseApi.<SanctionResponse>builder()
                .success(true)
                .message("Loan sanctioned successfully")
                .data(response)
                .build();
    }

    /*
     * Fetch the latest sanction letter for a deal.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseApi<SanctionResponse> getSanctionByDealId(
            Integer dealId) {

        LoanDeal loanDeal = loanDealRepository
                .findById(dealId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan deal not found with ID: " + dealId
                        )
                );

        SanctionLetter sanctionLetter =
                sanctionLetterRepository
                        .findTopByDealIdOrderBySanctionIdDesc(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sanction letter not found for deal: "
                                                + dealId
                                )
                        );

        SanctionResponse response = buildSanctionResponse(
                sanctionLetter,
                loanDeal,
                "Sanction details fetched successfully"
        );

        return ResponseApi.<SanctionResponse>builder()
                .success(true)
                .message("Sanction details fetched successfully")
                .data(response)
                .build();
    }

    /*
     * Fetch one sanction letter using its sanction ID.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseApi<SanctionResponse> getSanctionById(
            Integer sanctionId) {

        SanctionLetter sanctionLetter =
                sanctionLetterRepository
                        .findById(sanctionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sanction letter not found with ID: "
                                                + sanctionId
                                )
                        );

        LoanDeal loanDeal = loanDealRepository
                .findById(sanctionLetter.getDealId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan deal not found with ID: "
                                        + sanctionLetter.getDealId()
                        )
                );

        SanctionResponse response = buildSanctionResponse(
                sanctionLetter,
                loanDeal,
                "Sanction details fetched successfully"
        );

        return ResponseApi.<SanctionResponse>builder()
                .success(true)
                .message("Sanction details fetched successfully")
                .data(response)
                .build();
    }

    /*
     * NEW: Fetch all historical sanction letters.
     * Newest sanction ID appears first.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseApi<List<SanctionResponse>> getAllSanctions() {

        List<SanctionLetter> sanctionLetters =
                sanctionLetterRepository
                        .findAllByOrderBySanctionIdDesc();

        List<SanctionResponse> responses = new ArrayList<>();

        for (SanctionLetter sanctionLetter : sanctionLetters) {

            LoanDeal loanDeal = loanDealRepository
                    .findById(sanctionLetter.getDealId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Loan deal not found for sanction ID: "
                                            + sanctionLetter.getSanctionId()
                            )
                    );

            SanctionResponse response = buildSanctionResponse(
                    sanctionLetter,
                    loanDeal,
                    "Sanction details fetched successfully"
            );

            responses.add(response);
        }

        return ResponseApi.<List<SanctionResponse>>builder()
                .success(true)
                .message("Sanction history fetched successfully")
                .data(responses)
                .build();
    }

    /*
     * Determine annual interest rate.
     */
    private BigDecimal getInterestRate(LoanType loanType) {

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
     * Calculate EMI using the existing formula.
     */
    private BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal annualInterestRate,
            Integer tenureMonths) {

        double principalValue = principal.doubleValue();
        double annualRate = annualInterestRate.doubleValue();

        // Convert annual percentage into monthly decimal rate.
        double monthlyRate = annualRate / 12 / 100;

        double power = Math.pow(
                1 + monthlyRate,
                tenureMonths
        );

        double emi = principalValue
                * monthlyRate
                * power
                / (power - 1);

        return BigDecimal.valueOf(emi)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /*
     * Convert entity data into the existing response DTO.
     */
    private SanctionResponse buildSanctionResponse(
            SanctionLetter sanctionLetter,
            LoanDeal loanDeal,
            String message) {

        return SanctionResponse.builder()
                .sanctionId(sanctionLetter.getSanctionId())
                .dealId(loanDeal.getDealId())
                .customerId(loanDeal.getCustomerId())
                .loanType(loanDeal.getLoanType())
                .approvedLoanAmount(sanctionLetter.getLoanAmount())
                .interestRate(sanctionLetter.getInterestRate())
                .tenureMonths(sanctionLetter.getTenureMonths())
                .emiAmount(sanctionLetter.getEmiAmount())
                .emiDay(loanDeal.getEmiDay())
                .dealStatus(loanDeal.getDealStatus())
                .createdAt(sanctionLetter.getCreatedAt())
                .message(message)
                .build();
    }
}
