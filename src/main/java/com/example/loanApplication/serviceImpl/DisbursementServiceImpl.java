
package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.DisbursementRequest;
import com.example.loanApplication.dto.DisbursementResponse;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.entity.Disbursement;
import com.example.loanApplication.entity.LoanDeal;
import com.example.loanApplication.enumeration.DisbursementStatus;
import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.exception.InvalidLoanDealException;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.DisbursementRepository;
import com.example.loanApplication.repository.LoanDealRepository;
import com.example.loanApplication.service.DisbursementService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisbursementServiceImpl implements DisbursementService {

    private final DisbursementRepository disbursementRepository;
    private final LoanDealRepository loanDealRepository;

    private static final List<String> ALLOWED_BANK_PARTNERS = List.of(
            "Bank of India",
            "HDFC Bank",
            "ICICI Bank",
            "Bank of Maharashtra",
            "State Bank of India",
            "Axis Bank"
    );

    /*
     * CREATE DISBURSEMENT
     *
     * SANCTIONED
     *     -> Create Disbursement
     *     -> Save approved loan amount
     *     -> Save selected bank partner and planned date
     *     -> Status = PENDING
     *     -> LoanDeal = DISBURSEMENT_PENDING
     */
    @Override
    @Transactional
    public ResponseApi<DisbursementResponse> createDisbursement(
            DisbursementRequest request) {

        if (request == null) {
            throw new InvalidLoanDealException(
                    "Disbursement request is required."
            );
        }

        if (request.getDealId() == null) {
            throw new InvalidLoanDealException(
                    "Deal ID is required."
            );
        }

        if (request.getDisbursementDate() == null) {
            throw new InvalidLoanDealException(
                    "Disbursement date is required."
            );
        }

        if (request.getBankName() == null
                || request.getBankName().isBlank()) {
            throw new InvalidLoanDealException(
                    "Bank partner is required."
            );
        }

        // 1. Find the loan deal.
        LoanDeal loanDeal = loanDealRepository
                .findById(request.getDealId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan deal not found with ID: "
                                + request.getDealId()
                ));

        // 2. Only sanctioned loans can be disbursed.
        if (loanDeal.getDealStatus() != LoanDealStatus.SANCTIONED) {
            throw new InvalidLoanDealException(
                    "Disbursement can be created only for a sanctioned loan."
            );
        }

        // 3. Validate approved loan amount.
        if (loanDeal.getApprovedAmount() == null
                || loanDeal.getApprovedAmount().signum() <= 0) {
            throw new InvalidLoanDealException(
                    "Approved loan amount is not available."
            );
        }

        // 4. Validate and normalize the selected bank.
        final String requestedBank = request.getBankName().trim();

        final String selectedBank = ALLOWED_BANK_PARTNERS.stream()
                .filter(bank -> bank.equalsIgnoreCase(requestedBank))
                .findFirst()
                .orElseThrow(() -> new InvalidLoanDealException(
                        "Please select a valid bank partner."
                ));

        // 5. Validate the planned disbursement date.
        if (request.getDisbursementDate().isBefore(LocalDate.now())) {
            throw new InvalidLoanDealException(
                    "Disbursement date cannot be in the past."
            );
        }

        // 6. Validate customer bank account details.
        if (loanDeal.getBankAccountNumber() == null
                || loanDeal.getBankAccountNumber().isBlank()) {
            throw new InvalidLoanDealException(
                    "Bank account number is not available for disbursement."
            );
        }

        if (loanDeal.getIfscCode() == null
                || loanDeal.getIfscCode().isBlank()) {
            throw new InvalidLoanDealException(
                    "IFSC code is not available for disbursement."
            );
        }

        // 7. Create the disbursement.
        // Planned date is separate from the actual completion timestamp.
        Disbursement disbursement = Disbursement.builder()
                .dealId(loanDeal.getDealId())
                .amount(loanDeal.getApprovedAmount())
                .bankPartner(selectedBank)
                .bankAccountNumber(loanDeal.getBankAccountNumber())
                .ifscCode(loanDeal.getIfscCode())
                .plannedDisbursementDate(
                        request.getDisbursementDate()
                )
                .disbursementDate(null)
                .status(DisbursementStatus.PENDING)
                .build();

        Disbursement savedDisbursement =
                disbursementRepository.save(disbursement);

        // 8. Update the loan deal status.
        loanDeal.setDealStatus(LoanDealStatus.DISBURSEMENT_PENDING);
        loanDealRepository.save(loanDeal);

        // 9. Prepare response.
        DisbursementResponse response = buildDisbursementResponse(
                savedDisbursement,
                loanDeal,
                "Disbursement created successfully"
        );

        return ResponseApi.<DisbursementResponse>builder()
                .success(true)
                .message("Disbursement created successfully")
                .data(response)
                .build();
    }

    /*
     * COMPLETE DISBURSEMENT
     *
     * PENDING / PROCESSING
     *     -> COMPLETED
     *     -> Save actual completion timestamp
     *     -> LoanDeal = DISBURSED
     */
    @Override
    @Transactional
    public ResponseApi<DisbursementResponse> completeDisbursement(
            Integer disbursementId) {

        // 1. Find the disbursement.
        Disbursement disbursement = disbursementRepository
                .findById(disbursementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found with ID: "
                                + disbursementId
                ));

        // 2. Prevent duplicate completion.
        if (disbursement.getStatus() == DisbursementStatus.COMPLETED) {
            throw new InvalidLoanDealException(
                    "Disbursement is already completed."
            );
        }

        // 3. Only pending or processing records can be completed.
        if (disbursement.getStatus() != DisbursementStatus.PENDING
                && disbursement.getStatus()
                != DisbursementStatus.PROCESSING) {
            throw new InvalidLoanDealException(
                    "Only pending or processing disbursement can be completed."
            );
        }

        // 4. Find the associated loan deal.
        LoanDeal loanDeal = loanDealRepository
                .findById(disbursement.getDealId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan deal not found with ID: "
                                + disbursement.getDealId()
                ));

        // 5. Validate the loan deal status.
        if (loanDeal.getDealStatus()
                != LoanDealStatus.DISBURSEMENT_PENDING) {
            throw new InvalidLoanDealException(
                    "Loan deal is not pending for disbursement."
            );
        }

        // 6. Capture the actual disbursement completion timestamp.
        LocalDateTime completedAt = LocalDateTime.now();

// 7. Mark the disbursement as completed.
        disbursement.setStatus(DisbursementStatus.COMPLETED);
        disbursement.setDisbursementDate(completedAt);

        Disbursement savedDisbursement =
                disbursementRepository.save(disbursement);

// 8. Update the loan deal status and its disbursement date.
        loanDeal.setDealStatus(LoanDealStatus.DISBURSED);

        loanDeal.setPreferredDisbursementDate(
                completedAt.toLocalDate()
        );

        loanDealRepository.save(loanDeal);

        // 9. Prepare response.
        DisbursementResponse response = buildDisbursementResponse(
                savedDisbursement,
                loanDeal,
                "Disbursement completed successfully"
        );

        return ResponseApi.<DisbursementResponse>builder()
                .success(true)
                .message("Disbursement completed successfully")
                .data(response)
                .build();
    }

    /*
     * GET DISBURSEMENT BY ID
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseApi<DisbursementResponse> getDisbursementById(
            Integer disbursementId) {

        Disbursement disbursement = disbursementRepository
                .findById(disbursementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found with ID: "
                                + disbursementId
                ));

        LoanDeal loanDeal = loanDealRepository
                .findById(disbursement.getDealId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan deal not found with ID: "
                                + disbursement.getDealId()
                ));

        DisbursementResponse response = buildDisbursementResponse(
                disbursement,
                loanDeal,
                "Disbursement fetched successfully"
        );

        return ResponseApi.<DisbursementResponse>builder()
                .success(true)
                .message("Disbursement fetched successfully")
                .data(response)
                .build();
    }

    /*
     * GET LATEST DISBURSEMENT BY DEAL ID
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseApi<DisbursementResponse> getLatestDisbursementByDealId(
            Integer dealId) {

        Disbursement disbursement = disbursementRepository
                .findTopByDealIdOrderByDisbursementIdDesc(dealId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found for deal ID: " + dealId
                ));

        LoanDeal loanDeal = loanDealRepository
                .findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan deal not found with ID: " + dealId
                ));

        DisbursementResponse response = buildDisbursementResponse(
                disbursement,
                loanDeal,
                "Latest disbursement fetched successfully"
        );

        return ResponseApi.<DisbursementResponse>builder()
                .success(true)
                .message("Latest disbursement fetched successfully")
                .data(response)
                .build();
    }

    /*
     * GET DISBURSEMENT HISTORY WITH PAGINATION
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PageResponse<DisbursementResponse>>
    getDisbursementsByDealId(Integer dealId, int page, int size) {

        validatePagination(page, size);

        LoanDeal loanDeal = loanDealRepository
                .findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan deal not found with ID: " + dealId
                ));

        Pageable pageable = PageRequest.of(page, size);

        Page<Disbursement> disbursementPage = disbursementRepository
                .findByDealIdOrderByDisbursementIdDesc(dealId, pageable);

        if (disbursementPage.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No disbursement records found for deal ID: " + dealId
            );
        }

        List<DisbursementResponse> content = disbursementPage
                .getContent()
                .stream()
                .map(disbursement -> buildDisbursementResponse(
                        disbursement,
                        loanDeal,
                        "Disbursement fetched successfully"
                ))
                .toList();

        PageResponse<DisbursementResponse> pageResponse =
                PageResponse.<DisbursementResponse>builder()
                        .content(content)
                        .pageNumber(disbursementPage.getNumber())
                        .pageSize(disbursementPage.getSize())
                        .totalElements(disbursementPage.getTotalElements())
                        .totalPages(disbursementPage.getTotalPages())
                        .first(disbursementPage.isFirst())
                        .last(disbursementPage.isLast())
                        .build();

        return ResponseApi.<PageResponse<DisbursementResponse>>builder()
                .success(true)
                .message("Disbursement history fetched successfully")
                .data(pageResponse)
                .build();
    }

    /*
     * RESPONSE MAPPER
     */
    private DisbursementResponse buildDisbursementResponse(
            Disbursement disbursement,
            LoanDeal loanDeal,
            String message) {

        return DisbursementResponse.builder()
                .disbursementId(disbursement.getDisbursementId())
                .dealId(disbursement.getDealId())
                .customerId(loanDeal.getCustomerId())
                .disbursedAmount(disbursement.getAmount())
                .bankName(disbursement.getBankPartner())
                .bankAccountNumber(disbursement.getBankAccountNumber())
                .accountHolderName(loanDeal.getAccountHolderName())
                .ifscCode(disbursement.getIfscCode())
                .disbursementStatus(disbursement.getStatus())
                .dealStatus(loanDeal.getDealStatus())
                .plannedDisbursementDate(
                        disbursement.getPlannedDisbursementDate()
                )
                .disbursedAt(disbursement.getDisbursementDate())
                .message(message)
                .build();
    }

    /*
     * PAGINATION VALIDATION
     */
    private void validatePagination(int page, int size) {

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

        if (size > 100) {
            throw new InvalidLoanDealException(
                    "Page size cannot be greater than 100."
            );
        }
    }
}
