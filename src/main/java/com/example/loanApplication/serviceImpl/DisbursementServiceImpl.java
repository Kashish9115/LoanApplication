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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisbursementServiceImpl implements DisbursementService {

    private final DisbursementRepository disbursementRepository;
    private final LoanDealRepository loanDealRepository;


    /*
     * ============================================================
     * CREATE DISBURSEMENT
     * ============================================================
     *
     * Flow:
     *
     * SANCTIONED
     *      ↓
     * Create Disbursement
     *      ↓
     * Read Approved Amount
     *      ↓
     * Read Bank Details from LoanDeal
     *      ↓
     * Status = PENDING
     *      ↓
     * LoanDeal = DISBURSEMENT_PENDING
     */
    @Override
    @Transactional
    public ResponseApi<DisbursementResponse> createDisbursement(
            DisbursementRequest request
    ) {

        /*
         * STEP 1
         * Find loan deal.
         */
        LoanDeal loanDeal =
                loanDealRepository
                        .findById(request.getDealId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with ID: "
                                                + request.getDealId()
                                )
                        );


        /*
         * STEP 2
         * Disbursement can start
         * only after sanction.
         */
        if (loanDeal.getDealStatus()
                != LoanDealStatus.SANCTIONED) {

            throw new InvalidLoanDealException(
                    "Disbursement can be created only for a sanctioned loan."
            );
        }


        /*
         * STEP 3
         * Approved amount must exist.
         */
        if (loanDeal.getApprovedAmount() == null
                ||
                loanDeal.getApprovedAmount().signum() <= 0) {

            throw new InvalidLoanDealException(
                    "Approved loan amount is not available."
            );
        }


        /*
         * STEP 4
         * Validate Bank Name.
         *
         * BankPartner will be populated
         * using LoanDeal.bankName.
         */
        if (loanDeal.getBankName() == null
                ||
                loanDeal.getBankName().isBlank()) {

            throw new InvalidLoanDealException(
                    "Bank name is not available for disbursement."
            );
        }


        /*
         * STEP 5
         * Validate Bank Account Number.
         */
        if (loanDeal.getBankAccountNumber() == null
                ||
                loanDeal.getBankAccountNumber().isBlank()) {

            throw new InvalidLoanDealException(
                    "Bank account number is not available for disbursement."
            );
        }


        /*
         * STEP 6
         * Validate IFSC Code.
         */
        if (loanDeal.getIfscCode() == null
                ||
                loanDeal.getIfscCode().isBlank()) {

            throw new InvalidLoanDealException(
                    "IFSC code is not available for disbursement."
            );
        }


        /*
         * STEP 7
         * Preferred disbursement date
         * should already exist in LoanDeal.
         */
        if (loanDeal.getPreferredDisbursementDate() == null) {

            throw new InvalidLoanDealException(
                    "Preferred disbursement date is not available."
            );
        }


        /*
         * STEP 8
         * Create Disbursement.
         *
         * Important:
         *
         * BankPartner
         * =
         * LoanDeal.bankName
         *
         * DisbursementDate stays NULL
         * because amount is not yet transferred.
         */
        Disbursement disbursement =
                Disbursement.builder()
                        .dealId(
                                loanDeal.getDealId()
                        )
                        .amount(
                                loanDeal.getApprovedAmount()
                        )
                        .bankPartner(
                                loanDeal.getBankName()
                        )
                        .bankAccountNumber(
                                loanDeal.getBankAccountNumber()
                        )
                        .ifscCode(
                                loanDeal.getIfscCode()
                        )
                        .status(
                                DisbursementStatus.PENDING
                        )
                        .disbursementDate(
                                null
                        )
                        .build();


        /*
         * STEP 9
         * Save disbursement.
         */
        Disbursement savedDisbursement =
                disbursementRepository.save(
                        disbursement
                );


        /*
         * STEP 10
         * Update loan deal status.
         */
        loanDeal.setDealStatus(
                LoanDealStatus.DISBURSEMENT_PENDING
        );

        loanDealRepository.save(
                loanDeal
        );


        /*
         * STEP 11
         * Prepare response.
         */
        DisbursementResponse response =
                buildDisbursementResponse(
                        savedDisbursement,
                        loanDeal,
                        "Disbursement created successfully"
                );


        return ResponseApi
                .<DisbursementResponse>builder()
                .success(true)
                .message(
                        "Disbursement created successfully"
                )
                .data(response)
                .build();
    }


    /*
     * ============================================================
     * COMPLETE DISBURSEMENT
     * ============================================================
     *
     * PENDING
     *    ↓
     * Transfer Amount
     *    ↓
     * COMPLETED
     *    ↓
     * Save actual DisbursementDate
     *    ↓
     * LoanDeal = DISBURSED
     */
    @Override
    @Transactional
    public ResponseApi<DisbursementResponse> completeDisbursement(
            Integer disbursementId
    ) {

        /*
         * STEP 1
         * Find disbursement.
         */
        Disbursement disbursement =
                disbursementRepository
                        .findById(disbursementId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Disbursement not found with ID: "
                                                + disbursementId
                                )
                        );


        /*
         * STEP 2
         * Prevent already completed disbursement.
         */
        if (disbursement.getStatus()
                == DisbursementStatus.COMPLETED) {

            throw new InvalidLoanDealException(
                    "Disbursement is already completed."
            );
        }


        /*
         * STEP 3
         * Only PENDING or PROCESSING
         * disbursement can be completed.
         */
        if (disbursement.getStatus()
                != DisbursementStatus.PENDING
                &&
                disbursement.getStatus()
                        != DisbursementStatus.PROCESSING) {

            throw new InvalidLoanDealException(
                    "Only pending or processing disbursement can be completed."
            );
        }


        /*
         * STEP 4
         * Find associated LoanDeal.
         */
        LoanDeal loanDeal =
                loanDealRepository
                        .findById(
                                disbursement.getDealId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with ID: "
                                                + disbursement.getDealId()
                                )
                        );


        /*
         * STEP 5
         * LoanDeal should be waiting
         * for disbursement.
         */
        if (loanDeal.getDealStatus()
                != LoanDealStatus.DISBURSEMENT_PENDING) {

            throw new InvalidLoanDealException(
                    "Loan deal is not pending for disbursement."
            );
        }


        /*
         * STEP 6
         * Mark disbursement completed.
         */
        disbursement.setStatus(
                DisbursementStatus.COMPLETED
        );


        /*
         * STEP 7
         * Save ACTUAL disbursement
         * date and time.
         */
        disbursement.setDisbursementDate(
                LocalDateTime.now()
        );


        /*
         * STEP 8
         * Save updated disbursement.
         */
        Disbursement savedDisbursement =
                disbursementRepository.save(
                        disbursement
                );


        /*
         * STEP 9
         * Update LoanDeal status.
         */
        loanDeal.setDealStatus(
                LoanDealStatus.DISBURSED
        );

        loanDealRepository.save(
                loanDeal
        );


        /*
         * STEP 10
         * Prepare response.
         */
        DisbursementResponse response =
                buildDisbursementResponse(
                        savedDisbursement,
                        loanDeal,
                        "Disbursement completed successfully"
                );


        return ResponseApi
                .<DisbursementResponse>builder()
                .success(true)
                .message(
                        "Disbursement completed successfully"
                )
                .data(response)
                .build();
    }


    /*
     * ============================================================
     * GET DISBURSEMENT BY ID
     * ============================================================
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseApi<DisbursementResponse> getDisbursementById(
            Integer disbursementId
    ) {

        Disbursement disbursement =
                disbursementRepository
                        .findById(disbursementId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Disbursement not found with ID: "
                                                + disbursementId
                                )
                        );


        LoanDeal loanDeal =
                loanDealRepository
                        .findById(
                                disbursement.getDealId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with ID: "
                                                + disbursement.getDealId()
                                )
                        );


        DisbursementResponse response =
                buildDisbursementResponse(
                        disbursement,
                        loanDeal,
                        "Disbursement fetched successfully"
                );


        return ResponseApi
                .<DisbursementResponse>builder()
                .success(true)
                .message(
                        "Disbursement fetched successfully"
                )
                .data(response)
                .build();
    }


    /*
     * ============================================================
     * GET LATEST DISBURSEMENT BY DEAL
     * ============================================================
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseApi<DisbursementResponse>
    getLatestDisbursementByDealId(
            Integer dealId
    ) {

        Disbursement disbursement =
                disbursementRepository
                        .findTopByDealIdOrderByDisbursementIdDesc(
                                dealId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Disbursement not found for deal ID: "
                                                + dealId
                                )
                        );


        LoanDeal loanDeal =
                loanDealRepository
                        .findById(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with ID: "
                                                + dealId
                                )
                        );


        DisbursementResponse response =
                buildDisbursementResponse(
                        disbursement,
                        loanDeal,
                        "Latest disbursement fetched successfully"
                );


        return ResponseApi
                .<DisbursementResponse>builder()
                .success(true)
                .message(
                        "Latest disbursement fetched successfully"
                )
                .data(response)
                .build();
    }


    /*
     * ============================================================
     * GET DISBURSEMENT HISTORY WITH PAGINATION
     * ============================================================
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PageResponse<DisbursementResponse>>
    getDisbursementsByDealId(
            Integer dealId,
            int page,
            int size
    ) {

        validatePagination(
                page,
                size
        );


        /*
         * Make sure deal exists.
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


        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );


        Page<Disbursement> disbursementPage =
                disbursementRepository
                        .findByDealIdOrderByDisbursementIdDesc(
                                dealId,
                                pageable
                        );


        if (disbursementPage.isEmpty()) {

            throw new ResourceNotFoundException(
                    "No disbursement records found for deal ID: "
                            + dealId
            );
        }


        List<DisbursementResponse> content =
                disbursementPage
                        .getContent()
                        .stream()
                        .map(disbursement ->
                                buildDisbursementResponse(
                                        disbursement,
                                        loanDeal,
                                        "Disbursement fetched successfully"
                                )
                        )
                        .toList();


        PageResponse<DisbursementResponse> pageResponse =
                PageResponse
                        .<DisbursementResponse>builder()
                        .content(
                                content
                        )
                        .pageNumber(
                                disbursementPage.getNumber()
                        )
                        .pageSize(
                                disbursementPage.getSize()
                        )
                        .totalElements(
                                disbursementPage.getTotalElements()
                        )
                        .totalPages(
                                disbursementPage.getTotalPages()
                        )
                        .first(
                                disbursementPage.isFirst()
                        )
                        .last(
                                disbursementPage.isLast()
                        )
                        .build();


        return ResponseApi
                .<PageResponse<DisbursementResponse>>builder()
                .success(true)
                .message(
                        "Disbursement history fetched successfully"
                )
                .data(
                        pageResponse
                )
                .build();
    }


    /*
     * ============================================================
     * RESPONSE MAPPER
     * ============================================================
     */
    private DisbursementResponse buildDisbursementResponse(
            Disbursement disbursement,
            LoanDeal loanDeal,
            String message
    ) {

        return DisbursementResponse
                .builder()
                .disbursementId(
                        disbursement.getDisbursementId()
                )
                .dealId(
                        disbursement.getDealId()
                )
                .customerId(
                        loanDeal.getCustomerId()
                )
                .disbursedAmount(
                        disbursement.getAmount()
                )
                .bankName(
                        disbursement.getBankPartner()
                )
                .bankAccountNumber(
                        disbursement.getBankAccountNumber()
                )
                .accountHolderName(
                        loanDeal.getAccountHolderName()
                )
                .ifscCode(
                        disbursement.getIfscCode()
                )
                .disbursementStatus(
                        disbursement.getStatus()
                )

                /*
                 * Keeping your existing DTO field
                 * name "disbursedAt".
                 *
                 * Entity:
                 * DisbursementDate
                 *
                 * Response:
                 * disbursedAt
                 */
                .disbursedAt(
                        disbursement.getDisbursementDate()
                )
                .dealStatus(
                        loanDeal.getDealStatus()
                )
                .message(
                        message
                )
                .build();
    }


    /*
     * ============================================================
     * PAGINATION VALIDATION
     * ============================================================
     */
    private void validatePagination(
            int page,
            int size
    ) {

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