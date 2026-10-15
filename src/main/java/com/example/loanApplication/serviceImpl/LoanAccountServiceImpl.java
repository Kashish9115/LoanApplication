
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
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanAccountServiceImpl implements LoanAccountService {

    private final LoanAccountRepository loanAccountRepository;
    private final LoanDealRepository loanDealRepository;

    @Override
    @Transactional
    public ResponseApi<LoanAccountResponse> createLoanAccount(
            Integer dealId) {

        log.info("Starting loan account creation for dealId: {}", dealId);

        LoanDeal loanDeal = loanDealRepository
                .findById(dealId)
                .orElseThrow(() -> {
                    log.warn("Loan deal not found. dealId: {}", dealId);
                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: " + dealId
                    );
                });

        if (loanDeal.getDealStatus() != LoanDealStatus.DISBURSED) {
            log.warn(
                    "Loan account creation rejected. Deal is not disbursed. dealId: {}, status: {}",
                    dealId,
                    loanDeal.getDealStatus()
            );

            throw new InvalidLoanDealException(
                    "Loan account can be created only after loan disbursement."
            );
        }

        loanAccountRepository.findByDealId(dealId)
                .ifPresent(existingAccount -> {
                    log.warn(
                            "Loan account already exists for dealId: {}, loanAccountId: {}",
                            dealId,
                            existingAccount.getLoanAccountId()
                    );

                    throw new InvalidLoanDealException(
                            "Loan account already exists for deal ID: "
                                    + dealId
                    );
                });

        if (loanDeal.getApprovedAmount() == null
                || loanDeal.getApprovedAmount().signum() <= 0) {

            log.warn(
                    "Invalid approved amount for loan account creation. dealId: {}",
                    dealId
            );

            throw new InvalidLoanDealException(
                    "Approved loan amount is not available."
            );
        }

        if (loanDeal.getInterestRate() == null) {
            log.warn("Interest rate is missing. dealId: {}", dealId);

            throw new InvalidLoanDealException(
                    "Interest rate is not available for this loan."
            );
        }

        if (loanDeal.getEmiAmount() == null) {
            log.warn("EMI amount is missing. dealId: {}", dealId);

            throw new InvalidLoanDealException(
                    "EMI amount is not available for this loan."
            );
        }

        if (loanDeal.getPreferredDisbursementDate() == null) {
            log.warn("Disbursement date is missing. dealId: {}", dealId);

            throw new InvalidLoanDealException(
                    "Disbursement date is not available for this loan."
            );
        }

        String loanAccountNo =
                generateLoanAccountNumber(loanDeal.getDealId());

        LoanAccount loanAccount = LoanAccount.builder()
                .dealId(loanDeal.getDealId())
                .customerId(loanDeal.getCustomerId())
                .loanAccountNo(loanAccountNo)
                .loanAmount(loanDeal.getApprovedAmount())
                .outstandingAmount(loanDeal.getApprovedAmount())
                .interestRate(loanDeal.getInterestRate())
                .tenureMonths(loanDeal.getTenureMonths())
                .emiAmount(loanDeal.getEmiAmount())
                .emiDay(loanDeal.getEmiDay())
                .status(LoanAccountStatus.ACTIVE)
                .disbursementDate(
                        loanDeal.getPreferredDisbursementDate()
                )
                .createdAt(LocalDateTime.now())
                .build();

        LoanAccount savedLoanAccount =
                loanAccountRepository.save(loanAccount);

        log.info(
                "Loan account created successfully. loanAccountId: {}, loanAccountNo: {}, dealId: {}, customerId: {}",
                savedLoanAccount.getLoanAccountId(),
                savedLoanAccount.getLoanAccountNo(),
                savedLoanAccount.getDealId(),
                savedLoanAccount.getCustomerId()
        );

        LoanAccountResponse response = buildLoanAccountResponse(
                savedLoanAccount,
                loanDeal,
                "Loan account created successfully"
        );

        return ResponseApi.<LoanAccountResponse>builder()
                .success(true)
                .message("Loan account created successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PageResponse<LoanAccountResponse>> getAllLoanAccounts(
            int page,
            int size) {

        log.info(
                "Fetching all loan accounts. page: {}, size: {}",
                page,
                size
        );

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<LoanAccount> loanAccountPage =
                loanAccountRepository
                        .findAllByOrderByLoanAccountIdDesc(pageable);

        List<LoanAccountResponse> content =
                loanAccountPage.getContent()
                        .stream()
                        .map(loanAccount -> {

                            LoanDeal loanDeal =
                                    loanDealRepository
                                            .findById(loanAccount.getDealId())
                                            .orElseThrow(() -> {
                                                log.warn(
                                                        "Loan deal not found for loanAccountId: {}, dealId: {}",
                                                        loanAccount.getLoanAccountId(),
                                                        loanAccount.getDealId()
                                                );

                                                return new ResourceNotFoundException(
                                                        "Loan deal not found with ID: "
                                                                + loanAccount.getDealId()
                                                );
                                            });

                            return buildLoanAccountResponse(
                                    loanAccount,
                                    loanDeal,
                                    "Loan account fetched successfully"
                            );
                        })
                        .toList();

        PageResponse<LoanAccountResponse> pageResponse =
                PageResponse.<LoanAccountResponse>builder()
                        .content(content)
                        .pageNumber(loanAccountPage.getNumber())
                        .pageSize(loanAccountPage.getSize())
                        .totalElements(
                                loanAccountPage.getTotalElements()
                        )
                        .totalPages(
                                loanAccountPage.getTotalPages()
                        )
                        .first(loanAccountPage.isFirst())
                        .last(loanAccountPage.isLast())
                        .build();

        log.info(
                "Loan accounts fetched successfully. returnedRecords: {}, totalElements: {}",
                content.size(),
                loanAccountPage.getTotalElements()
        );

        return ResponseApi.<PageResponse<LoanAccountResponse>>builder()
                .success(true)
                .message("All loan accounts fetched successfully")
                .data(pageResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<LoanAccountResponse> getLoanAccountById(
            Integer loanAccountId) {

        log.info("Fetching loan account by ID: {}", loanAccountId);

        LoanAccount loanAccount = loanAccountRepository
                .findById(loanAccountId)
                .orElseThrow(() -> {
                    log.warn(
                            "Loan account not found. loanAccountId: {}",
                            loanAccountId
                    );

                    return new ResourceNotFoundException(
                            "Loan account not found with ID: "
                                    + loanAccountId
                    );
                });

        LoanDeal loanDeal = loanDealRepository
                .findById(loanAccount.getDealId())
                .orElseThrow(() -> {
                    log.warn(
                            "Loan deal not found for loanAccountId: {}, dealId: {}",
                            loanAccountId,
                            loanAccount.getDealId()
                    );

                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: "
                                    + loanAccount.getDealId()
                    );
                });

        LoanAccountResponse response = buildLoanAccountResponse(
                loanAccount,
                loanDeal,
                "Loan account fetched successfully"
        );

        log.info(
                "Loan account fetched successfully. loanAccountId: {}, dealId: {}",
                loanAccountId,
                loanAccount.getDealId()
        );

        return ResponseApi.<LoanAccountResponse>builder()
                .success(true)
                .message("Loan account fetched successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<LoanAccountResponse> getLoanAccountByDealId(
            Integer dealId) {

        log.info("Fetching loan account for dealId: {}", dealId);

        LoanAccount loanAccount = loanAccountRepository
                .findByDealId(dealId)
                .orElseThrow(() -> {
                    log.warn(
                            "Loan account not found for dealId: {}",
                            dealId
                    );

                    return new ResourceNotFoundException(
                            "Loan account not found for deal ID: " + dealId
                    );
                });

        LoanDeal loanDeal = loanDealRepository
                .findById(dealId)
                .orElseThrow(() -> {
                    log.warn("Loan deal not found. dealId: {}", dealId);

                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: " + dealId
                    );
                });

        LoanAccountResponse response = buildLoanAccountResponse(
                loanAccount,
                loanDeal,
                "Loan account fetched successfully"
        );

        log.info(
                "Loan account fetched successfully for dealId: {}, loanAccountId: {}",
                dealId,
                loanAccount.getLoanAccountId()
        );

        return ResponseApi.<LoanAccountResponse>builder()
                .success(true)
                .message("Loan account fetched successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<LoanAccountResponse> getLatestLoanAccountByCustomerId(
            Integer customerId) {

        log.info(
                "Fetching latest loan account for customerId: {}",
                customerId
        );

        LoanAccount loanAccount = loanAccountRepository
                .findTopByCustomerIdOrderByLoanAccountIdDesc(customerId)
                .orElseThrow(() -> {
                    log.warn(
                            "Loan account not found for customerId: {}",
                            customerId
                    );

                    return new ResourceNotFoundException(
                            "Loan account not found for customer: " + customerId
                    );
                });

        LoanDeal loanDeal = loanDealRepository
                .findById(loanAccount.getDealId())
                .orElseThrow(() -> {
                    log.warn(
                            "Loan deal not found for loanAccountId: {}, dealId: {}",
                            loanAccount.getLoanAccountId(),
                            loanAccount.getDealId()
                    );

                    return new ResourceNotFoundException(
                            "Loan deal not found with ID: "
                                    + loanAccount.getDealId()
                    );
                });

        LoanAccountResponse response = buildLoanAccountResponse(
                loanAccount,
                loanDeal,
                "Latest loan account fetched successfully"
        );

        log.info(
                "Latest loan account fetched successfully. customerId: {}, loanAccountId: {}",
                customerId,
                loanAccount.getLoanAccountId()
        );

        return ResponseApi.<LoanAccountResponse>builder()
                .success(true)
                .message("Latest loan account fetched successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PageResponse<LoanAccountResponse>>
    getLoanAccountsByCustomerId(
            Integer customerId,
            int page,
            int size) {

        log.info(
                "Fetching loan accounts for customerId: {}, page: {}, size: {}",
                customerId,
                page,
                size
        );

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<LoanAccount> loanAccountPage =
                loanAccountRepository
                        .findByCustomerIdOrderByLoanAccountIdDesc(
                                customerId,
                                pageable
                        );

        if (loanAccountPage.isEmpty()) {
            log.warn(
                    "No loan accounts found for customerId: {}",
                    customerId
            );

            throw new ResourceNotFoundException(
                    "No loan accounts found for customer: " + customerId
            );
        }

        List<LoanAccountResponse> content =
                loanAccountPage.getContent()
                        .stream()
                        .map(loanAccount -> {

                            LoanDeal loanDeal =
                                    loanDealRepository
                                            .findById(loanAccount.getDealId())
                                            .orElseThrow(() -> {
                                                log.warn(
                                                        "Loan deal not found for loanAccountId: {}, dealId: {}",
                                                        loanAccount.getLoanAccountId(),
                                                        loanAccount.getDealId()
                                                );

                                                return new ResourceNotFoundException(
                                                        "Loan deal not found with ID: "
                                                                + loanAccount.getDealId()
                                                );
                                            });

                            return buildLoanAccountResponse(
                                    loanAccount,
                                    loanDeal,
                                    "Loan account fetched successfully"
                            );
                        })
                        .toList();

        PageResponse<LoanAccountResponse> pageResponse =
                PageResponse.<LoanAccountResponse>builder()
                        .content(content)
                        .pageNumber(loanAccountPage.getNumber())
                        .pageSize(loanAccountPage.getSize())
                        .totalElements(
                                loanAccountPage.getTotalElements()
                        )
                        .totalPages(
                                loanAccountPage.getTotalPages()
                        )
                        .first(loanAccountPage.isFirst())
                        .last(loanAccountPage.isLast())
                        .build();

        log.info(
                "Customer loan accounts fetched successfully. customerId: {}, returnedRecords: {}, totalElements: {}",
                customerId,
                content.size(),
                loanAccountPage.getTotalElements()
        );

        return ResponseApi.<PageResponse<LoanAccountResponse>>builder()
                .success(true)
                .message("Customer loan accounts fetched successfully")
                .data(pageResponse)
                .build();
    }

    private String generateLoanAccountNumber(Integer dealId) {

        return String.format("LA%010d", dealId);
    }

    private void validatePagination(int page, int size) {

        if (page < 0) {
            log.warn(
                    "Invalid pagination: negative page number. page: {}, size: {}",
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

    private LoanAccountResponse buildLoanAccountResponse(
            LoanAccount loanAccount,
            LoanDeal loanDeal,
            String message) {

        return LoanAccountResponse.builder()
                .loanAccountId(loanAccount.getLoanAccountId())
                .dealId(loanAccount.getDealId())
                .customerId(loanAccount.getCustomerId())
                .loanAccountNo(loanAccount.getLoanAccountNo())
                .loanType(loanDeal.getLoanType())
                .loanAmount(loanAccount.getLoanAmount())
                .outstandingAmount(loanAccount.getOutstandingAmount())
                .interestRate(loanAccount.getInterestRate())
                .tenureMonths(loanAccount.getTenureMonths())
                .emiAmount(loanAccount.getEmiAmount())
                .emiDay(loanAccount.getEmiDay())
                .status(loanAccount.getStatus())
                .createdAt(loanAccount.getCreatedAt())
                .message(message)
                .build();
    }
}
