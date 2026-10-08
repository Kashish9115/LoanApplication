package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.LoanAccountResponse;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.entity.LoanAccount;
import com.example.loanApplication.entity.LoanDeal;
import com.example.loanApplication.enumeration.LoanAccountStatus;
import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.exception.InvalidLoanDealException;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.LoanAccountRepository;
import com.example.loanApplication.repository.LoanDealRepository;
import com.example.loanApplication.service.LoanAccountService;

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
public class LoanAccountServiceImpl implements LoanAccountService {

    private final LoanAccountRepository loanAccountRepository;
    private final LoanDealRepository loanDealRepository;


    @Override
    @Transactional
    public ResponseApi<LoanAccountResponse> createLoanAccount(
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
         * Loan account can be created
         * only after successful disbursement.
         */
        if (loanDeal.getDealStatus()
                != LoanDealStatus.DISBURSED) {

            throw new InvalidLoanDealException(
                    "Loan account can be created only after loan disbursement."
            );
        }


        /*
         * STEP 3
         * Prevent duplicate loan account
         * for same DealId.
         */
        loanAccountRepository
                .findByDealId(dealId)
                .ifPresent(existingAccount -> {

                    throw new InvalidLoanDealException(
                            "Loan account already exists for deal ID: "
                                    + dealId
                    );
                });


        /*
         * STEP 4
         * Validate approved amount.
         */
        if (loanDeal.getApprovedAmount() == null
                ||
                loanDeal.getApprovedAmount().signum() <= 0) {

            throw new InvalidLoanDealException(
                    "Approved loan amount is not available."
            );
        }


        /*
         * STEP 5
         * Interest rate should already
         * be generated during sanction.
         */
        if (loanDeal.getInterestRate() == null) {

            throw new InvalidLoanDealException(
                    "Interest rate is not available for this loan."
            );
        }


        /*
         * STEP 6
         * EMI should already
         * be generated during sanction.
         */
        if (loanDeal.getEmiAmount() == null) {

            throw new InvalidLoanDealException(
                    "EMI amount is not available for this loan."
            );
        }


        /*
         * STEP 7
         * Preferred disbursement date
         * must be available.
         */
        if (loanDeal.getPreferredDisbursementDate() == null) {

            throw new InvalidLoanDealException(
                    "Disbursement date is not available for this loan."
            );
        }


        /*
         * STEP 8
         * Generate Loan Account Number.
         *
         * Example:
         * Deal ID = 1
         * Loan Account No = LA0000000001
         */
        String loanAccountNo =
                generateLoanAccountNumber(
                        loanDeal.getDealId()
                );


        /*
         * STEP 9
         * Create Loan Account.
         *
         * LoanAmount
         * =
         * ApprovedAmount
         *
         * OutstandingPrincipal
         * =
         * ApprovedAmount
         *
         * DisbursementDate
         * =
         * PreferredDisbursementDate
         */
        LoanAccount loanAccount =
                LoanAccount.builder()
                        .dealId(
                                loanDeal.getDealId()
                        )
                        .customerId(
                                loanDeal.getCustomerId()
                        )
                        .loanAccountNo(
                                loanAccountNo
                        )
                        .loanAmount(
                                loanDeal.getApprovedAmount()
                        )
                        .outstandingAmount(
                                loanDeal.getApprovedAmount()
                        )
                        .interestRate(
                                loanDeal.getInterestRate()
                        )
                        .tenureMonths(
                                loanDeal.getTenureMonths()
                        )
                        .emiAmount(
                                loanDeal.getEmiAmount()
                        )
                        .emiDay(
                                loanDeal.getEmiDay()
                        )
                        .status(
                                LoanAccountStatus.ACTIVE
                        )
                        .disbursementDate(
                                loanDeal.getPreferredDisbursementDate()
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();


        /*
         * STEP 10
         * Save Loan Account.
         */
        LoanAccount savedLoanAccount =
                loanAccountRepository.save(
                        loanAccount
                );


        /*
         * STEP 11
         * Prepare response.
         */
        LoanAccountResponse response =
                buildLoanAccountResponse(
                        savedLoanAccount,
                        loanDeal,
                        "Loan account created successfully"
                );


        return ResponseApi
                .<LoanAccountResponse>builder()
                .success(true)
                .message(
                        "Loan account created successfully"
                )
                .data(response)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseApi<LoanAccountResponse> getLoanAccountById(
            Integer loanAccountId
    ) {

        LoanAccount loanAccount =
                loanAccountRepository
                        .findById(loanAccountId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan account not found with ID: "
                                                + loanAccountId
                                )
                        );


        LoanDeal loanDeal =
                loanDealRepository
                        .findById(
                                loanAccount.getDealId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with ID: "
                                                + loanAccount.getDealId()
                                )
                        );


        LoanAccountResponse response =
                buildLoanAccountResponse(
                        loanAccount,
                        loanDeal,
                        "Loan account fetched successfully"
                );


        return ResponseApi
                .<LoanAccountResponse>builder()
                .success(true)
                .message(
                        "Loan account fetched successfully"
                )
                .data(response)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseApi<LoanAccountResponse> getLoanAccountByDealId(
            Integer dealId
    ) {

        LoanAccount loanAccount =
                loanAccountRepository
                        .findByDealId(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan account not found for deal ID: "
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


        LoanAccountResponse response =
                buildLoanAccountResponse(
                        loanAccount,
                        loanDeal,
                        "Loan account fetched successfully"
                );


        return ResponseApi
                .<LoanAccountResponse>builder()
                .success(true)
                .message(
                        "Loan account fetched successfully"
                )
                .data(response)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseApi<LoanAccountResponse>
    getLatestLoanAccountByCustomerId(
            Integer customerId
    ) {

        LoanAccount loanAccount =
                loanAccountRepository
                        .findTopByCustomerIdOrderByLoanAccountIdDesc(
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan account not found for customer: "
                                                + customerId
                                )
                        );


        LoanDeal loanDeal =
                loanDealRepository
                        .findById(
                                loanAccount.getDealId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with ID: "
                                                + loanAccount.getDealId()
                                )
                        );


        LoanAccountResponse response =
                buildLoanAccountResponse(
                        loanAccount,
                        loanDeal,
                        "Latest loan account fetched successfully"
                );


        return ResponseApi
                .<LoanAccountResponse>builder()
                .success(true)
                .message(
                        "Latest loan account fetched successfully"
                )
                .data(response)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PageResponse<LoanAccountResponse>>
    getLoanAccountsByCustomerId(
            Integer customerId,
            int page,
            int size
    ) {

        /*
         * Validate pagination.
         */
        validatePagination(
                page,
                size
        );


        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );


        Page<LoanAccount> loanAccountPage =
                loanAccountRepository
                        .findByCustomerIdOrderByLoanAccountIdDesc(
                                customerId,
                                pageable
                        );


        if (loanAccountPage.isEmpty()) {

            throw new ResourceNotFoundException(
                    "No loan accounts found for customer: "
                            + customerId
            );
        }


        List<LoanAccountResponse> content =
                loanAccountPage
                        .getContent()
                        .stream()
                        .map(loanAccount -> {

                            LoanDeal loanDeal =
                                    loanDealRepository
                                            .findById(
                                                    loanAccount.getDealId()
                                            )
                                            .orElseThrow(() ->
                                                    new ResourceNotFoundException(
                                                            "Loan deal not found with ID: "
                                                                    + loanAccount.getDealId()
                                                    )
                                            );


                            return buildLoanAccountResponse(
                                    loanAccount,
                                    loanDeal,
                                    "Loan account fetched successfully"
                            );
                        })
                        .toList();


        PageResponse<LoanAccountResponse> pageResponse =
                PageResponse
                        .<LoanAccountResponse>builder()
                        .content(
                                content
                        )
                        .pageNumber(
                                loanAccountPage.getNumber()
                        )
                        .pageSize(
                                loanAccountPage.getSize()
                        )
                        .totalElements(
                                loanAccountPage.getTotalElements()
                        )
                        .totalPages(
                                loanAccountPage.getTotalPages()
                        )
                        .first(
                                loanAccountPage.isFirst()
                        )
                        .last(
                                loanAccountPage.isLast()
                        )
                        .build();


        return ResponseApi
                .<PageResponse<LoanAccountResponse>>builder()
                .success(true)
                .message(
                        "Customer loan accounts fetched successfully"
                )
                .data(
                        pageResponse
                )
                .build();
    }


    /*
     * Generate Loan Account Number.
     *
     * Deal ID = 1
     * LA0000000001
     *
     * Deal ID = 25
     * LA0000000025
     */
    private String generateLoanAccountNumber(
            Integer dealId
    ) {

        return String.format(
                "LA%010d",
                dealId
        );
    }


    /*
     * Validate pagination values.
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


    /*
     * Convert LoanAccount entity
     * into LoanAccountResponse DTO.
     */
    private LoanAccountResponse buildLoanAccountResponse(
            LoanAccount loanAccount,
            LoanDeal loanDeal,
            String message
    ) {

        return LoanAccountResponse.builder()
                .loanAccountId(
                        loanAccount.getLoanAccountId()
                )
                .dealId(
                        loanAccount.getDealId()
                )
                .customerId(
                        loanAccount.getCustomerId()
                )
                .loanAccountNo(
                        loanAccount.getLoanAccountNo()
                )
                .loanType(
                        loanDeal.getLoanType()
                )
                .loanAmount(
                        loanAccount.getLoanAmount()
                )
                .outstandingAmount(
                        loanAccount.getOutstandingAmount()
                )
                .interestRate(
                        loanAccount.getInterestRate()
                )
                .tenureMonths(
                        loanAccount.getTenureMonths()
                )
                .emiAmount(
                        loanAccount.getEmiAmount()
                )
                .emiDay(
                        loanAccount.getEmiDay()
                )
                .status(
                        loanAccount.getStatus()
                )
                .createdAt(
                        loanAccount.getCreatedAt()
                )
                .message(
                        message
                )
                .build();
    }
}