
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
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SanctionServiceImpl implements SanctionService {

    private final LoanDealRepository loanDealRepository;
    private final SanctionLetterRepository sanctionLetterRepository;

    @Override
    @Transactional
    public ResponseApi<SanctionResponse> sanctionLoan(Integer dealId) {

        log.info("Starting loan sanction process for dealId: {}", dealId);

        LoanDeal loanDeal = loanDealRepository
                .findById(dealId)
                .orElseThrow(() -> {
                    log.warn("Loan deal not found for sanction. dealId: {}", dealId);
                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: " + dealId
                    );
                });

        if (loanDeal.getDealStatus() != LoanDealStatus.AUTO_APPROVED
                && loanDeal.getDealStatus() != LoanDealStatus.APPROVED) {

            log.warn(
                    "Sanction rejected because deal is not approved. dealId: {}, status: {}",
                    dealId,
                    loanDeal.getDealStatus()
            );

            throw new InvalidLoanDealException(
                    "Only APPROVED or AUTO_APPROVED loan deals can be sanctioned."
            );
        }

        if (loanDeal.getApprovedAmount() == null
                || loanDeal.getApprovedAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            log.warn(
                    "Sanction rejected because approved amount is missing or invalid. dealId: {}",
                    dealId
            );

            throw new InvalidLoanDealException(
                    "Approved loan amount is not available for this deal."
            );
        }

        if (loanDeal.getTenureMonths() == null
                || loanDeal.getTenureMonths() <= 0) {

            log.warn(
                    "Sanction rejected because loan tenure is invalid. dealId: {}, tenureMonths: {}",
                    dealId,
                    loanDeal.getTenureMonths()
            );

            throw new InvalidLoanDealException(
                    "Valid loan tenure is required before sanction."
            );
        }

        BigDecimal interestRate =
                getInterestRate(loanDeal.getLoanType());

        BigDecimal emiAmount = calculateEmi(
                loanDeal.getApprovedAmount(),
                interestRate,
                loanDeal.getTenureMonths()
        );

        log.info(
                "Sanction terms calculated. dealId: {}, loanType: {}, approvedAmount: {}, interestRate: {}, tenureMonths: {}, emiAmount: {}",
                dealId,
                loanDeal.getLoanType(),
                loanDeal.getApprovedAmount(),
                interestRate,
                loanDeal.getTenureMonths(),
                emiAmount
        );

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

        log.info(
                "Sanction letter saved successfully. sanctionId: {}, dealId: {}",
                savedSanctionLetter.getSanctionId(),
                dealId
        );

        loanDeal.setInterestRate(interestRate);
        loanDeal.setEmiAmount(emiAmount);
        loanDeal.setDealStatus(LoanDealStatus.SANCTIONED);

        LoanDeal updatedLoanDeal =
                loanDealRepository.save(loanDeal);

        log.info(
                "Loan deal updated to SANCTIONED. dealId: {}, customerId: {}",
                updatedLoanDeal.getDealId(),
                updatedLoanDeal.getCustomerId()
        );

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

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<SanctionResponse> getSanctionByDealId(
            Integer dealId) {

        log.info("Fetching sanction details for dealId: {}", dealId);

        LoanDeal loanDeal = loanDealRepository
                .findById(dealId)
                .orElseThrow(() -> {
                    log.warn("Loan deal not found for dealId: {}", dealId);
                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: " + dealId
                    );
                });

        SanctionLetter sanctionLetter =
                sanctionLetterRepository
                        .findTopByDealIdOrderBySanctionIdDesc(dealId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Sanction letter not found for dealId: {}",
                                    dealId
                            );
                            return new ResourceNotFoundException(
                                    "Sanction letter not found for deal: "
                                            + dealId
                            );
                        });

        SanctionResponse response = buildSanctionResponse(
                sanctionLetter,
                loanDeal,
                "Sanction details fetched successfully"
        );

        log.info(
                "Sanction details fetched successfully. sanctionId: {}, dealId: {}",
                sanctionLetter.getSanctionId(),
                dealId
        );

        return ResponseApi.<SanctionResponse>builder()
                .success(true)
                .message("Sanction details fetched successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<SanctionResponse> getSanctionById(
            Integer sanctionId) {

        log.info("Fetching sanction by sanctionId: {}", sanctionId);

        SanctionLetter sanctionLetter =
                sanctionLetterRepository
                        .findById(sanctionId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Sanction letter not found. sanctionId: {}",
                                    sanctionId
                            );
                            return new ResourceNotFoundException(
                                    "Sanction letter not found with ID: "
                                            + sanctionId
                            );
                        });

        LoanDeal loanDeal = loanDealRepository
                .findById(sanctionLetter.getDealId())
                .orElseThrow(() -> {
                    log.warn(
                            "Loan deal not found for sanctionId: {}, dealId: {}",
                            sanctionId,
                            sanctionLetter.getDealId()
                    );
                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: "
                                    + sanctionLetter.getDealId()
                    );
                });

        SanctionResponse response = buildSanctionResponse(
                sanctionLetter,
                loanDeal,
                "Sanction details fetched successfully"
        );

        log.info(
                "Sanction fetched successfully. sanctionId: {}, dealId: {}",
                sanctionId,
                loanDeal.getDealId()
        );

        return ResponseApi.<SanctionResponse>builder()
                .success(true)
                .message("Sanction details fetched successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<List<SanctionResponse>> getAllSanctions() {

        log.info("Fetching all sanction letters");

        List<SanctionLetter> sanctionLetters =
                sanctionLetterRepository
                        .findAllByOrderBySanctionIdDesc();

        List<SanctionResponse> responses = new ArrayList<>();

        for (SanctionLetter sanctionLetter : sanctionLetters) {

            LoanDeal loanDeal = loanDealRepository
                    .findById(sanctionLetter.getDealId())
                    .orElseThrow(() -> {
                        log.warn(
                                "Loan deal not found for sanctionId: {}, dealId: {}",
                                sanctionLetter.getSanctionId(),
                                sanctionLetter.getDealId()
                        );
                        return new ResourceNotFoundException(
                                "Loan deal not found for sanction ID: "
                                        + sanctionLetter.getSanctionId()
                        );
                    });

            SanctionResponse response = buildSanctionResponse(
                    sanctionLetter,
                    loanDeal,
                    "Sanction details fetched successfully"
            );

            responses.add(response);
        }

        log.info(
                "Sanction history fetched successfully. totalRecords: {}",
                responses.size()
        );

        return ResponseApi.<List<SanctionResponse>>builder()
                .success(true)
                .message("Sanction history fetched successfully")
                .data(responses)
                .build();
    }

    private BigDecimal getInterestRate(LoanType loanType) {

        if (loanType == null) {
            log.warn("Loan type is missing while calculating sanction interest rate");

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

        log.warn("Unsupported loan type for sanction: {}", loanType);

        throw new InvalidLoanDealException(
                "Unsupported loan type: " + loanType
        );
    }

    private BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal annualInterestRate,
            Integer tenureMonths) {

        double principalValue = principal.doubleValue();
        double annualRate = annualInterestRate.doubleValue();

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
