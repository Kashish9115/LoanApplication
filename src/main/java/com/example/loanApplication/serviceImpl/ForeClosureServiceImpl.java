package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.*;
import com.example.loanApplication.entity.ForeClosureRequest;
import com.example.loanApplication.entity.LoanAccount;
import com.example.loanApplication.entity.LoanClosure;
import com.example.loanApplication.entity.LoanPayments;
import com.example.loanApplication.enumeration.ForeClosureStatus;
import com.example.loanApplication.enumeration.LoanAccountStatus;
import com.example.loanApplication.repository.ForeClosureRequestRepository;
import com.example.loanApplication.repository.LoanAccountRepository;
import com.example.loanApplication.repository.LoanClosureRepository;
import com.example.loanApplication.repository.LoanPaymentRepository;
import com.example.loanApplication.service.ForeClosureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForeClosureServiceImpl implements ForeClosureService {
    private  final LoanAccountRepository loanAccountRepository;
    private  final ForeClosureRequestRepository foreClosureRequestRepository;
   private final   LoanClosureRepository  loanClosureRepository;
   private  final LoanPaymentRepository loanPaymentRepository;
    ModelMapper modelMapper=new ModelMapper();

    @Override
    public ResponseApi<List<Integer>> getAllActiveLoanAccounts(Integer customerId) {

        log.info("Customer ID received: {}", customerId);

        List<LoanAccount> allLoans =
                loanAccountRepository.findByCustomerId(customerId);

        log.info("Total loans found: {}", allLoans.size());

        allLoans.forEach(loan ->
                log.info("Loan ID: {}, Status: {}",
                        loan.getLoanAccountId(),
                        loan.getStatus())
        );

        List<Integer> activeLoanIds = allLoans.stream()
                .filter(loan -> loan.getStatus() == LoanAccountStatus.ACTIVE)
                .map(LoanAccount::getLoanAccountId)
                .toList();

        return new ResponseApi<>(
                true,
                "Active loan accounts fetched successfully",
                activeLoanIds
        );
    }

    @Override
    public ResponseApi<LoanAccountDetailsDto> getLoanAccountDetails(
            Integer loanAccountId) {

        log.info("Fetching active loan account details for loanAccountId: {}",
                loanAccountId);

        LoanAccount loanAccount = loanAccountRepository
                .findByLoanAccountIdAndStatus(
                        loanAccountId,
                        LoanAccountStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Active loan account not found with id: "
                                        + loanAccountId
                        )
                );

        LocalDate nextEmiDate =
                calculateNextEmiDate(loanAccount.getEmiDay());

        LoanAccountDetailsDto dto = new LoanAccountDetailsDto();
        dto.setCustomerId(loanAccount.getCustomerId());
        dto.setLoanAccountId(loanAccount.getLoanAccountId());
        dto.setLoanAccountNo(loanAccount.getLoanAccountNo());
        dto.setOutstandingAmount(loanAccount.getOutstandingAmount());
        dto.setNextEmiDate(nextEmiDate);

        log.info("Loan account details fetched successfully for loanAccountId: {}",
                loanAccountId);

        return new ResponseApi<>(
                true,
                "Loan account details fetched successfully",
                dto
        );
    }
    private LocalDate calculateNextEmiDate(Integer emiDay) {

        if (emiDay == null || emiDay < 1) {
            throw new RuntimeException("Invalid EMI day");
        }

        LocalDate today = LocalDate.now();

        YearMonth currentMonth = YearMonth.from(today);

        int validDay = Math.min(
                emiDay,
                currentMonth.lengthOfMonth()
        );

        LocalDate currentMonthEmiDate =
                currentMonth.atDay(validDay);

        if (!today.isAfter(currentMonthEmiDate)) {
            return currentMonthEmiDate;
        }

        YearMonth nextMonth = currentMonth.plusMonths(1);

        int nextValidDay = Math.min(
                emiDay,
                nextMonth.lengthOfMonth()
        );

        return nextMonth.atDay(nextValidDay);
    }

    @Override
    public ResponseApi<ForeClosureRequest> requestFullClosure(
            Integer loanAccountId,
            FullClosureRequestDto request) {

        log.info("Full foreclosure request received for loanAccountId: {}",
                loanAccountId);

        // 1. Find active loan
        LoanAccount loanAccount = loanAccountRepository
                .findByLoanAccountIdAndStatus(
                        loanAccountId,
                        LoanAccountStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Active loan account not found with id: "
                                        + loanAccountId
                        )
                );

        // 2. Validate expected closure date
        if (request.getExpectedClosureDate() == null) {
            throw new RuntimeException(
                    "Expected closure date is required"
            );
        }

        // 3. Expected closure date should not be before today
        if (request.getExpectedClosureDate()
                .isBefore(LocalDate.now())) {

            throw new RuntimeException(
                    "Expected closure date cannot be in the past"
            );
        }

        // 4. Create foreclosure request
        ForeClosureRequest foreclosureRequest =
                new ForeClosureRequest();

        foreclosureRequest.setLoanAccountId(
                loanAccount.getLoanAccountId()
        );

        // FULL
        foreclosureRequest.setForeClosureType("FULL");

        // Full foreclosure amount = outstanding principal
        foreclosureRequest.setForeClosureAmount(
                loanAccount.getOutstandingAmount()
        );

        // Full closure mein partial amount nahi hoga
        foreclosureRequest.setPartialAmount(null);

        // Request created time
        foreclosureRequest.setRequestedDate(
                LocalDateTime.now()
        );

        // Convert LocalDate -> LocalDateTime
        foreclosureRequest.setExpectedClosureDate(
                request.getExpectedClosureDate().atStartOfDay()
        );

        // Customer reason
        foreclosureRequest.setReason(
                request.getReason()
        );

        // Initial status
        foreclosureRequest.setStatus("PENDING");

        // Initial payment state
        foreclosureRequest.setIsPaid(false);

        foreclosureRequest.setApprovedDate(null);
        foreclosureRequest.setPaidDate(null);
        foreclosureRequest.setClosedBy(null);

        // 5. Save request
        ForeClosureRequest savedRequest =
                foreClosureRequestRepository.save(
                        foreclosureRequest
                );

        log.info(
                "Full foreclosure request created successfully. requestId: {}",
                savedRequest.getRequestId()
        );

        return new ResponseApi<>(
                true,
                "Full foreclosure request submitted successfully",
                savedRequest
        );
    }

    @Override
    public ResponseApi<ForeClosureRequest> requestPartialClosure(Integer loanAccountId, PartialClosureRequestDto request) {
        log.info("Partial foreclosure request for loanAccountId: {}",
                loanAccountId);

        // 1. Get active loan
        LoanAccount loanAccount = loanAccountRepository
                .findByLoanAccountIdAndStatus(
                        loanAccountId,
                        LoanAccountStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Active loan account not found with id: "
                                        + loanAccountId
                        )
                );

        // 2. Validate months
        if (request.getMonths() == null ||
                request.getMonths() <= 0) {

            throw new RuntimeException(
                    "Number of months must be greater than zero"
            );
        }

        // 3. Get EMI amount
        BigDecimal emiAmount = loanAccount.getEmiAmount();

        if (emiAmount == null ||
                emiAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "EMI amount is not available for this loan"
            );
        }

        // 4. Calculate partial amount
        BigDecimal partialAmount = emiAmount.multiply(
                BigDecimal.valueOf(request.getMonths())
        );

        // 5. Check partial amount against outstanding
        if (partialAmount.compareTo(
                loanAccount.getOutstandingAmount()) > 0) {

            throw new RuntimeException(
                    "Calculated partial amount cannot be greater than outstanding amount"
            );
        }

        // 6. Calculate remaining outstanding
        BigDecimal remainingOutstanding =
                loanAccount.getOutstandingAmount()
                        .subtract(partialAmount);

        log.info(
                "EMI: {}, Months: {}, Partial Amount: {}, Remaining Outstanding: {}",
                emiAmount,
                request.getMonths(),
                partialAmount,
                remainingOutstanding
        );

        // 7. Validate expected closure date
        if (request.getExpectedClosureDate() == null) {

            throw new RuntimeException(
                    "Expected closure date is required"
            );
        }

        if (request.getExpectedClosureDate()
                .isBefore(LocalDate.now())) {

            throw new RuntimeException(
                    "Expected closure date cannot be in the past"
            );
        }

        // 8. Create foreclosure request
        ForeClosureRequest foreclosureRequest =
                new ForeClosureRequest();

        foreclosureRequest.setLoanAccountId(
                loanAccount.getLoanAccountId()
        );

        foreclosureRequest.setForeClosureType("PARTIAL");

        foreclosureRequest.setMonths(
                request.getMonths()
        );

        foreclosureRequest.setForeClosureAmount(
                partialAmount
        );

        foreclosureRequest.setPartialAmount(
                partialAmount
        );

        foreclosureRequest.setRequestedDate(
                LocalDateTime.now()
        );

        foreclosureRequest.setExpectedClosureDate(
                request.getExpectedClosureDate().atStartOfDay()
        );

        foreclosureRequest.setReason(
                request.getReason()
        );

        foreclosureRequest.setStatus("PENDING");

        foreclosureRequest.setIsPaid(false);

        foreclosureRequest.setApprovedDate(null);
        foreclosureRequest.setPaidDate(null);
        foreclosureRequest.setClosedBy(null);

        // 9. Save request
        ForeClosureRequest savedRequest =
                foreClosureRequestRepository.save(
                        foreclosureRequest
                );

        log.info(
                "Partial foreclosure request created successfully. RequestId: {}",
                savedRequest.getRequestId()
        );

        return new ResponseApi<>(
                true,
                "Partial foreclosure request submitted successfully",
                savedRequest
        );

    }

    @Override
    public ResponseApi<List<CustomerForeClosureRequestDto>>
    getCustomerForeclosureRequests(Integer customerId) {

        log.info("Fetching foreclosure requests for customerId: {}",
                customerId);

        // Step 1: Customer ke saare loan accounts fetch karo
        List<LoanAccount> loanAccounts =
                loanAccountRepository.findByCustomerId(customerId);

        if (loanAccounts.isEmpty()) {
            return new ResponseApi<>(
                    true,
                    "No loan accounts found for this customer",
                    List.of()
            );
        }

        // Step 2: Loan account IDs nikalo
        List<Integer> loanAccountIds = loanAccounts.stream()
                .map(LoanAccount::getLoanAccountId)
                .toList();

        // Step 3: In loan accounts ki saari foreclosure requests fetch karo
        List<ForeClosureRequest> requests =
                foreClosureRequestRepository
                        .findByLoanAccountIdIn(loanAccountIds);

        // Step 4: Loan account number ke liye mapping banao
        Map<Integer, String> loanNumbers = loanAccounts.stream()
                .collect(Collectors.toMap(
                        LoanAccount::getLoanAccountId,
                        LoanAccount::getLoanAccountNo
                ));

        // Step 5: Entity ko DTO mein convert karo
        List<CustomerForeClosureRequestDto> response =
                requests.stream()
                        .map(request -> {
                            CustomerForeClosureRequestDto dto =
                                    modelMapper.map(
                                            request,
                                            CustomerForeClosureRequestDto.class
                                    );

                            dto.setLoanAccountNo(
                                    loanNumbers.get(
                                            request.getLoanAccountId()
                                    )
                            );

                            return dto;
                        })
                        .toList();

        return new ResponseApi<>(
                true,
                response.isEmpty()
                        ? "No foreclosure requests found"
                        : "Foreclosure requests fetched successfully",
                response
        );
    }

    @Override
    public ResponseApi<List<ForeClosureRequest>>
    getPendingForeclosureRequests() {

        log.info("Fetching pending foreclosure requests");


        List<ForeClosureRequest> requests =
                foreClosureRequestRepository.findByStatus(
                        ForeClosureStatus.PENDING.name()
                );

        log.info("Total pending requests found: {}", requests.size());

        return new ResponseApi<>(
                true,
                requests.isEmpty()
                        ? "No pending foreclosure requests found"
                        : "Pending foreclosure requests fetched successfully",
                requests
        );
    }


    @Override
    public ResponseApi<ForeClosureRequest> approveForeclosureRequest(
            Integer requestId) {

        log.info("Approving foreclosure request: {}", requestId);

        ForeClosureRequest request =
                foreClosureRequestRepository
                        .findByRequestIdAndStatus(requestId, "PENDING")
                        .orElse(null);

        if (request == null) {
            return new ResponseApi<>(
                    false,
                    "Pending foreclosure request not found",
                    null
            );
        }

        request.setStatus("APPROVED");
        request.setApprovedDate(LocalDateTime.now());

        ForeClosureRequest savedRequest =
                foreClosureRequestRepository.save(request);

        log.info("Foreclosure request {} approved successfully", requestId);

        return new ResponseApi<>(
                true,
                "Foreclosure request approved successfully",
                savedRequest
        );
    }


    @Override
    public ResponseApi<ForeClosureRequest> rejectForeclosureRequest(
            Integer requestId) {

        log.info("Rejecting foreclosure request: {}", requestId);

        ForeClosureRequest request =
                foreClosureRequestRepository
                        .findByRequestIdAndStatus(requestId, "PENDING")
                        .orElse(null);

        if (request == null) {
            return new ResponseApi<>(
                    false,
                    "Pending foreclosure request not found",
                    null
            );
        }

        request.setStatus("REJECTED");

        ForeClosureRequest savedRequest =
                foreClosureRequestRepository.save(request);

        log.info("Foreclosure request {} rejected successfully", requestId);

        return new ResponseApi<>(
                true,
                "Foreclosure request rejected successfully",
                savedRequest
        );
    }
    @Override
    @Transactional
    public void doneClosure(Integer requestId, BigDecimal paidAmount) {

        ForeClosureRequest request = foreClosureRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Record not found"));

        if (!"APPROVED".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException(
                    "Foreclosure request is not approved");
        }

        if (Boolean.TRUE.equals(request.getIsPaid())) {
            throw new IllegalArgumentException(
                    "Foreclosure request is already paid");
        }

        LoanAccount account = loanAccountRepository
                .findById(request.getLoanAccountId())
                .orElseThrow(null);

        BigDecimal outstanding = account.getOutstandingAmount();

        if (paidAmount == null
                || paidAmount.compareTo(BigDecimal.ZERO) <= 0
                || outstanding == null
                || paidAmount.compareTo(outstanding) > 0) {
            throw new IllegalArgumentException(
                    "Invalid foreclosure payment amount");
        }

        BigDecimal remainingAmount = outstanding.subtract(paidAmount);

        LoanClosure closure = new LoanClosure();
        closure.setLoanAccountId(account.getLoanAccountId());
        closure.setClosureType(request.getForeClosureType());
        closure.setFinalSettlementAmount(paidAmount);
        closure.setClosureDate(LocalDateTime.now());
        closure.setClosedBy(request.getClosedBy());
        closure.setClosureStatus("COMPLETED");

        if ("FULL".equalsIgnoreCase(request.getForeClosureType())) {

            if (remainingAmount.compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException(
                        "Full foreclosure payment must settle the outstanding amount");
            }

            account.setOutstandingAmount(BigDecimal.ZERO);
            account.setStatus(LoanAccountStatus.CLOSED);

            closure.setRemarks("Full foreclosure completed");

        } else if ("PARTIAL".equalsIgnoreCase(request.getForeClosureType())) {

            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Partial payment must leave a positive outstanding amount");
            }

            account.setOutstandingAmount(remainingAmount);
            account.setStatus(LoanAccountStatus.ACTIVE);

            closure.setFinalSettlementAmount(remainingAmount);
            closure.setRemarks("Remaining outstanding for EMI regeneration");

            // Call your existing EMI regeneration method here.
            // emiService.regenerateEmi(account, remainingAmount);

        } else {
            throw new IllegalArgumentException(
                    "Invalid foreclosure type");
        }

        loanClosureRepository.save(closure);
        loanAccountRepository.save(account);

        request.setIsPaid(true);
        request.setPaidDate(LocalDateTime.now());
        foreClosureRequestRepository.save(request);
    }

    @Override
    public ResponseApi<List<ForeClosurePaymentDto>>
    getApprovedForeclosurePayments(Integer customerId) {

        List<LoanAccount> accounts =
                loanAccountRepository.findByCustomerId(customerId);

        Map<Integer, LoanAccount> accountMap = accounts.stream()
                .collect(Collectors.toMap(
                        LoanAccount::getLoanAccountId,
                        account -> account
                ));

        List<Integer> loanIds = accounts.stream()
                .map(LoanAccount::getLoanAccountId)
                .toList();

        if (loanIds.isEmpty()) {
            return new ResponseApi<>(
                    true,
                    "No foreclosure payments found",
                    List.of()
            );
        }

        List<ForeClosurePaymentDto> result =
                foreClosureRequestRepository
                        .findByLoanAccountIdIn(loanIds)
                        .stream()
                        .filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
                        .filter(r -> !Boolean.TRUE.equals(r.getIsPaid()))
                        .filter(r -> {
                            LoanAccount account = accountMap.get(r.getLoanAccountId());

                            return account != null
                                    && account.getStatus() != LoanAccountStatus.CLOSED;
                        })
                        .filter(r -> {
                            LoanAccount account = accountMap.get(r.getLoanAccountId());

                            return account != null
                                    && account.getStatus() == LoanAccountStatus.ACTIVE
                                    && account.getOutstandingAmount() != null
                                    && account.getOutstandingAmount()
                                    .compareTo(BigDecimal.ZERO) > 0;
                        })
                        .map(r -> {
                            LoanAccount account =
                                    accountMap.get(r.getLoanAccountId());

                            ForeClosurePaymentDto dto =
                                    new ForeClosurePaymentDto();

                            dto.setForeClosureRequestId(r.getRequestId());
                            dto.setLoanAccountId(r.getLoanAccountId());
                            dto.setLoanAccountNo(account.getLoanAccountNo());
                            dto.setForeClosureType(r.getForeClosureType());
                            dto.setForeClosureAmount(r.getForeClosureAmount());
                            dto.setOutstandingAmount(account.getOutstandingAmount());
                            dto.setExpectedClosureDate(r.getExpectedClosureDate());
                            dto.setStatus(r.getStatus());
                            dto.setIsPaid(r.getIsPaid());
                            dto.setReason(r.getReason());

                            return dto;
                        })
                        .toList();

        return new ResponseApi<>(
                true,
                "Approved foreclosure payments fetched successfully",
                result
        );
    }

//    @Transactional
//    @Override
//    public LoanPaymentResponseDTO processForeClosurePayment(
//            ForeClosurePaymentRequestDto dto) {
//
//        // 1. Fetch the foreclosure request
//        ForeClosureRequest request = foreClosureRequestRepository
//                .findById(dto.getForeClosureRequestId())
//                .orElseThrow(() ->
//                        new IllegalArgumentException(
//                                "Foreclosure request not found: "
//                                        + dto.getForeClosureRequestId()));
//
//        // 2. Validate approval and payment status
//        if (!"APPROVED".equalsIgnoreCase(request.getStatus())) {
//            throw new IllegalArgumentException(
//                    "Foreclosure request is not approved");
//        }
//        // Fetch the loan account before processing payment
//        LoanAccount loanAccount = loanAccountRepository
//                .findById(request.getLoanAccountId())
//                .orElseThrow(() ->
//                        new IllegalArgumentException("Loan account not found"));
//
//// Prevent payment against a closed loan
//        if ("CLOSED".equalsIgnoreCase(String.valueOf(loanAccount.getStatus()))) {
//            throw new IllegalArgumentException(
//                    "Loan account is already closed");
//        }
//
//        if (Boolean.TRUE.equals(request.getIsPaid())) {
//            throw new IllegalArgumentException(
//                    "Foreclosure request is already paid");
//        }
//
//        // 3. Validate the amount against the approved request
//        BigDecimal approvedAmount =
//                "FULL".equalsIgnoreCase(request.getForeClosureType())
//                        ? request.getForeClosureAmount()
//                        : request.getPartialAmount();
//
//        if (dto.getPaymentAmount() == null
//                || approvedAmount == null
//                || dto.getPaymentAmount().compareTo(approvedAmount) != 0) {
//            throw new IllegalArgumentException(
//                    "Payment amount does not match the approved foreclosure amount");
//        }
//
//        // IMPORTANT:
//        // Call this method only AFTER actual Razorpay payment verification.
//        // Do not trust payment IDs or signatures supplied by the frontend alone.
//
//        // 4. Save the verified payment
//        LoanPayments payment = new LoanPayments();
//        payment.setLoanAccountId(request.getLoanAccountId());
//        payment.setPaymentAmount(dto.getPaymentAmount());
//        payment.setPaymentDate(LocalDateTime.now());
//        payment.setPaymentStatus("SUCCESS");
//        payment.setPaymentName(
//                "FULL".equalsIgnoreCase(request.getForeClosureType())
//                        ? "Full Foreclosure Payment"
//                        : "Partial Foreclosure Payment");
//
//        LoanPayments savedPayment = loanPaymentRepository.save(payment);
//
//        // 5. Complete foreclosure.
//        // This method handles outstanding amount and loan status.
//        doneClosure(
//                request.getRequestId(),
//                savedPayment.getPaymentAmount()
//        );
//
//        // 6. Return payment response
//        LoanPaymentResponseDTO response =
//                modelMapper.map(savedPayment, LoanPaymentResponseDTO.class);
//
//        response.setMessage(
//                "Foreclosure payment processed successfully");
//
//        return response;
//    }

    @Override
    public ResponseApi<Page<CustomerForeClosureRequestDto>> getCustomerForeclosureRequests(Integer customerId, Pageable pageable) {

            List<Integer> loanAccountIds =
                    loanAccountRepository.findLoanAccountIdsByCustomerId(customerId);

            if (loanAccountIds == null || loanAccountIds.isEmpty()) {
                return new ResponseApi<>(
                        true,
                        "No foreclosure requests found",
                        Page.empty(pageable)
                );
            }

            Page<ForeClosureRequest> requestPage =
                    foreClosureRequestRepository
                            .findByLoanAccountIdIn(loanAccountIds, pageable);

            Page<CustomerForeClosureRequestDto> dtoPage =
                    requestPage.map(request -> {
                        CustomerForeClosureRequestDto dto =
                                new CustomerForeClosureRequestDto();

                        // Yahan apne existing DTO mapping code ko rakhein.
                        // Example:
                        dto.setRequestId(request.getRequestId());
                        dto.setLoanAccountId(request.getLoanAccountId());
                        dto.setStatus(request.getStatus());

                        return dto;
                    });

            return new ResponseApi<>(
                    true,
                    "Foreclosure requests fetched successfully",
                    dtoPage
            );

    }


}
