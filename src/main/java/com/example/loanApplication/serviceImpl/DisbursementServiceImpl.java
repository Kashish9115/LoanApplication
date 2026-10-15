
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
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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

    @Override
    @Transactional
    public ResponseApi<DisbursementResponse> createDisbursement(
            DisbursementRequest request) {

        log.info("Starting disbursement creation");

        if (request == null) {
            log.warn("Disbursement creation rejected: request is null");
            throw new InvalidLoanDealException(
                    "Disbursement request is required."
            );
        }

        if (request.getDealId() == null) {
            log.warn("Disbursement creation rejected: dealId is missing");
            throw new InvalidLoanDealException(
                    "Deal ID is required."
            );
        }

        log.info(
                "Creating disbursement for dealId: {}",
                request.getDealId()
        );

        if (request.getDisbursementDate() == null) {
            log.warn(
                    "Disbursement date is missing for dealId: {}",
                    request.getDealId()
            );
            throw new InvalidLoanDealException(
                    "Disbursement date is required."
            );
        }

        if (request.getBankName() == null
                || request.getBankName().isBlank()) {
            log.warn(
                    "Bank partner is missing for dealId: {}",
                    request.getDealId()
            );
            throw new InvalidLoanDealException(
                    "Bank partner is required."
            );
        }

        LoanDeal loanDeal = loanDealRepository
                .findById(request.getDealId())
                .orElseThrow(() -> {
                    log.warn(
                            "Loan deal not found for disbursement. dealId: {}",
                            request.getDealId()
                    );
                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: "
                                    + request.getDealId()
                    );
                });

        if (loanDeal.getDealStatus() != LoanDealStatus.SANCTIONED) {
            log.warn(
                    "Disbursement rejected: loan is not sanctioned. dealId: {}, status: {}",
                    loanDeal.getDealId(),
                    loanDeal.getDealStatus()
            );
            throw new InvalidLoanDealException(
                    "Disbursement can be created only for a sanctioned loan."
            );
        }

        if (loanDeal.getApprovedAmount() == null
                || loanDeal.getApprovedAmount().signum() <= 0) {
            log.warn(
                    "Invalid approved amount for disbursement. dealId: {}",
                    loanDeal.getDealId()
            );
            throw new InvalidLoanDealException(
                    "Approved loan amount is not available."
            );
        }

        final String requestedBank = request.getBankName().trim();

        final String selectedBank = ALLOWED_BANK_PARTNERS.stream()
                .filter(bank -> bank.equalsIgnoreCase(requestedBank))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn(
                            "Invalid bank partner selected for dealId: {}",
                            request.getDealId()
                    );
                    return new InvalidLoanDealException(
                            "Please select a valid bank partner."
                    );
                });

        if (request.getDisbursementDate().isBefore(LocalDate.now())) {
            log.warn(
                    "Past disbursement date rejected for dealId: {}, date: {}",
                    request.getDealId(),
                    request.getDisbursementDate()
            );
            throw new InvalidLoanDealException(
                    "Disbursement date cannot be in the past."
            );
        }

        if (loanDeal.getBankAccountNumber() == null
                || loanDeal.getBankAccountNumber().isBlank()) {
            log.warn(
                    "Bank account number missing for dealId: {}",
                    loanDeal.getDealId()
            );
            throw new InvalidLoanDealException(
                    "Bank account number is not available for disbursement."
            );
        }

        if (loanDeal.getIfscCode() == null
                || loanDeal.getIfscCode().isBlank()) {
            log.warn(
                    "IFSC code missing for dealId: {}",
                    loanDeal.getDealId()
            );
            throw new InvalidLoanDealException(
                    "IFSC code is not available for disbursement."
            );
        }

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

        log.info(
                "Disbursement record created. disbursementId: {}, dealId: {}, amount: {}, status: {}",
                savedDisbursement.getDisbursementId(),
                savedDisbursement.getDealId(),
                savedDisbursement.getAmount(),
                savedDisbursement.getStatus()
        );

        loanDeal.setDealStatus(LoanDealStatus.DISBURSEMENT_PENDING);
        loanDealRepository.save(loanDeal);

        log.info(
                "Loan deal status updated to DISBURSEMENT_PENDING. dealId: {}",
                loanDeal.getDealId()
        );

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

    @Override
    @Transactional
    public ResponseApi<DisbursementResponse> completeDisbursement(
            Integer disbursementId) {

        log.info(
                "Starting disbursement completion. disbursementId: {}",
                disbursementId
        );

        Disbursement disbursement = disbursementRepository
                .findById(disbursementId)
                .orElseThrow(() -> {
                    log.warn(
                            "Disbursement not found. disbursementId: {}",
                            disbursementId
                    );
                    return new ResourceNotFoundException(
                            "Disbursement not found with ID: "
                                    + disbursementId
                    );
                });

        if (disbursement.getStatus() == DisbursementStatus.COMPLETED) {
            log.warn(
                    "Disbursement is already completed. disbursementId: {}",
                    disbursementId
            );
            throw new InvalidLoanDealException(
                    "Disbursement is already completed."
            );
        }

        if (disbursement.getStatus() != DisbursementStatus.PENDING
                && disbursement.getStatus()
                != DisbursementStatus.PROCESSING) {

            log.warn(
                    "Cannot complete disbursement with status: {}. disbursementId: {}",
                    disbursement.getStatus(),
                    disbursementId
            );

            throw new InvalidLoanDealException(
                    "Only pending or processing disbursement can be completed."
            );
        }

        LoanDeal loanDeal = loanDealRepository
                .findById(disbursement.getDealId())
                .orElseThrow(() -> {
                    log.warn(
                            "Loan deal not found for disbursementId: {}, dealId: {}",
                            disbursementId,
                            disbursement.getDealId()
                    );
                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: "
                                    + disbursement.getDealId()
                    );
                });

        if (loanDeal.getDealStatus()
                != LoanDealStatus.DISBURSEMENT_PENDING) {

            log.warn(
                    "Loan deal is not pending for disbursement. dealId: {}, status: {}",
                    loanDeal.getDealId(),
                    loanDeal.getDealStatus()
            );

            throw new InvalidLoanDealException(
                    "Loan deal is not pending for disbursement."
            );
        }

        LocalDateTime completedAt = LocalDateTime.now();

        disbursement.setStatus(DisbursementStatus.COMPLETED);
        disbursement.setDisbursementDate(completedAt);

        Disbursement savedDisbursement =
                disbursementRepository.save(disbursement);

        loanDeal.setDealStatus(LoanDealStatus.DISBURSED);

        loanDeal.setPreferredDisbursementDate(
                completedAt.toLocalDate()
        );

        loanDealRepository.save(loanDeal);

        log.info(
                "Disbursement completed successfully. disbursementId: {}, dealId: {}, amount: {}, completedAt: {}",
                savedDisbursement.getDisbursementId(),
                savedDisbursement.getDealId(),
                savedDisbursement.getAmount(),
                completedAt
        );

        log.info(
                "Loan deal status updated to DISBURSED. dealId: {}",
                loanDeal.getDealId()
        );

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

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<DisbursementResponse> getDisbursementById(
            Integer disbursementId) {

        log.info(
                "Fetching disbursement by ID: {}",
                disbursementId
        );

        Disbursement disbursement = disbursementRepository
                .findById(disbursementId)
                .orElseThrow(() -> {
                    log.warn(
                            "Disbursement not found. disbursementId: {}",
                            disbursementId
                    );
                    return new ResourceNotFoundException(
                            "Disbursement not found with ID: "
                                    + disbursementId
                    );
                });

        LoanDeal loanDeal = loanDealRepository
                .findById(disbursement.getDealId())
                .orElseThrow(() -> {
                    log.warn(
                            "Loan deal not found for disbursementId: {}, dealId: {}",
                            disbursementId,
                            disbursement.getDealId()
                    );
                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: "
                                    + disbursement.getDealId()
                    );
                });

        DisbursementResponse response = buildDisbursementResponse(
                disbursement,
                loanDeal,
                "Disbursement fetched successfully"
        );

        log.info(
                "Disbursement fetched successfully. disbursementId: {}, dealId: {}",
                disbursement.getDisbursementId(),
                disbursement.getDealId()
        );

        return ResponseApi.<DisbursementResponse>builder()
                .success(true)
                .message("Disbursement fetched successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<DisbursementResponse> getLatestDisbursementByDealId(
            Integer dealId) {

        log.info(
                "Fetching latest disbursement for dealId: {}",
                dealId
        );

        Disbursement disbursement = disbursementRepository
                .findTopByDealIdOrderByDisbursementIdDesc(dealId)
                .orElseThrow(() -> {
                    log.warn(
                            "Disbursement not found for dealId: {}",
                            dealId
                    );
                    return new ResourceNotFoundException(
                            "Disbursement not found for deal ID: " + dealId
                    );
                });

        LoanDeal loanDeal = loanDealRepository
                .findById(dealId)
                .orElseThrow(() -> {
                    log.warn(
                            "Loan deal not found for dealId: {}",
                            dealId
                    );
                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: " + dealId
                    );
                });

        DisbursementResponse response = buildDisbursementResponse(
                disbursement,
                loanDeal,
                "Latest disbursement fetched successfully"
        );

        log.info(
                "Latest disbursement fetched successfully. disbursementId: {}, dealId: {}",
                disbursement.getDisbursementId(),
                dealId
        );

        return ResponseApi.<DisbursementResponse>builder()
                .success(true)
                .message("Latest disbursement fetched successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PageResponse<DisbursementResponse>>
    getDisbursementsByDealId(Integer dealId, int page, int size) {

        log.info(
                "Fetching disbursement history for dealId: {}, page: {}, size: {}",
                dealId,
                page,
                size
        );

        validatePagination(page, size);

        LoanDeal loanDeal = loanDealRepository
                .findById(dealId)
                .orElseThrow(() -> {
                    log.warn(
                            "Loan deal not found while fetching disbursement history. dealId: {}",
                            dealId
                    );
                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: " + dealId
                    );
                });

        Pageable pageable = PageRequest.of(page, size);

        Page<Disbursement> disbursementPage = disbursementRepository
                .findByDealIdOrderByDisbursementIdDesc(dealId, pageable);

        if (disbursementPage.isEmpty()) {
            log.warn(
                    "No disbursement records found for dealId: {}",
                    dealId
            );

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

        log.info(
                "Disbursement history fetched. dealId: {}, returnedRecords: {}, totalElements: {}",
                dealId,
                content.size(),
                disbursementPage.getTotalElements()
        );

        return ResponseApi.<PageResponse<DisbursementResponse>>builder()
                .success(true)
                .message("Disbursement history fetched successfully")
                .data(pageResponse)
                .build();
    }

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
            log.warn(
                    "Invalid pagination: page number cannot be negative. page: {}, size: {}",
                    page,
                    size
            );

            throw new InvalidLoanDealException(
                    "Page number cannot be negative."
            );
        }

        if (size <= 0) {
            log.warn(
                    "Invalid pagination: page size must be greater than zero. page: {}, size: {}",
                    page,
                    size
            );

            throw new InvalidLoanDealException(
                    "Page size must be greater than zero."
            );
        }

        if (size > 100) {
            log.warn(
                    "Invalid pagination: page size exceeds maximum. page: {}, size: {}",
                    page,
                    size
            );

            throw new InvalidLoanDealException(
                    "Page size cannot be greater than 100."
            );
        }
    }
}
