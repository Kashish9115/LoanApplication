package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.*;
import com.example.loanApplication.entity.ForeClosureRequest;
import com.example.loanApplication.entity.LoanAccount;
import com.example.loanApplication.enumeration.ForeClosureStatus;
import com.example.loanApplication.enumeration.LoanAccountStatus;
import com.example.loanApplication.repository.ForeClosureRequestRepository;
import com.example.loanApplication.repository.LoanAccountRepository;
import com.example.loanApplication.service.ForeClosureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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


}
