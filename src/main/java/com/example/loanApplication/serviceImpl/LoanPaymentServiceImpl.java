package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.EmiBreakDownDto;
import com.example.loanApplication.dto.ForeClosurePaymentRequestDto;
import com.example.loanApplication.dto.LoanPaymentRequestDTO;
import com.example.loanApplication.dto.LoanPaymentResponseDTO;
import com.example.loanApplication.entity.*;
import com.example.loanApplication.enumeration.LoanAccountStatus;
import com.example.loanApplication.enumeration.PaymentStatus;
import com.example.loanApplication.exception.InvalidPaymentAmountException;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.EmiSchedulesRepo;
import com.example.loanApplication.repository.ForeClosureRequestRepository;
import com.example.loanApplication.repository.LoanAccountRepository;
import com.example.loanApplication.repository.LoanPaymentRepository;
import com.example.loanApplication.service.LoanPaymentService;
import com.example.loanApplication.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoanPaymentServiceImpl implements LoanPaymentService {

    private final LoanPaymentRepository loanPaymentRepository;
    private  final LoanAccountRepository loanAccountRepo;
    private  final EmiSchedulesRepo emiSchedulesRepo;
    private  final  EmiScheduleServiceImpl emiService;
    private  final NotificationService notificationService;
  private  final ForeClosureRequestRepository foreClosureRequestRepository;
private  final ModelMapper modelMapper;
    @Override
    public List<LoanPaymentResponseDTO> getPaymentHistoryByAccountId(Integer LoanAccountId) {

        List<LoanPayments> loanpayments = loanPaymentRepository.findByLoanAccountIdOrderByPaymentDateDesc(LoanAccountId);


        return  loanpayments.stream().map(paymentHistory->modelMapper.map(paymentHistory, LoanPaymentResponseDTO.class)).toList();
    }


    @Override
    @Transactional
    public LoanPaymentResponseDTO processPaymentLedger(
            LoanPaymentRequestDTO dto) {

        if (dto == null
                || dto.getLoanAccountId() == null
                || dto.getPaymentAmount() == null
                || dto.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid payment request.");
        }

        // 1. Fetch loan account
        LoanAccount account = loanAccountRepo.findById(dto.getLoanAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found: " + dto.getLoanAccountId()
                ));

        // 2. Fetch pending EMIs in installment order
        List<EmiSchedules> pendingEmis =
                emiSchedulesRepo
                        .findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
                                account.getLoanAccountId(), "PENDING"
                        );

        if (pendingEmis.isEmpty()) {
            throw new IllegalStateException("No pending EMI available.");
        }

        LocalDate today = LocalDate.now();
        List<EmiSchedules> payableEmis;

        if (dto.getEmiScheduleId() == null) {

            // Combined mode: all pending EMIs due today or earlier
            payableEmis = pendingEmis.stream()
                    .filter(e -> e.getDueDate() != null)
                    .filter(e -> !e.getDueDate().isAfter(today))
                    .toList();

        } else {

            // Single EMI mode: only the earliest pending EMI can be paid
            EmiSchedules selected = pendingEmis.stream()
                    .filter(e -> e.getEmiScheduleId()
                            .equals(dto.getEmiScheduleId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pending EMI not found: " + dto.getEmiScheduleId()
                    ));

            EmiSchedules earliestPending = pendingEmis.get(0);

            if (!earliestPending.getEmiScheduleId()
                    .equals(selected.getEmiScheduleId())) {
                throw new IllegalStateException(
                        "Previous pending EMI exists. Use Pay All Due to clear all due EMIs."
                );
            }

            if (selected.getDueDate() == null
                    || selected.getDueDate().isAfter(today)) {
                throw new IllegalStateException("This EMI is not due yet.");
            }

            payableEmis = List.of(selected);
        }

        if (payableEmis.isEmpty()) {
            throw new IllegalStateException("No due EMI available for payment.");
        }

        // 3. Calculate total from backend data
        BigDecimal requiredAmount = BigDecimal.ZERO;
        BigDecimal principalRepaid = BigDecimal.ZERO;

        for (EmiSchedules emi : payableEmis) {

            EmiBreakDownDto breakdown =
                    emiService.getEmiBreakdown(emi.getEmiScheduleId());

            requiredAmount = requiredAmount.add(
                    breakdown.getTotalPayableAmount()
            );

            if (emi.getPrincipalAmount() == null
                    || emi.getPrincipalAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException(
                        "Invalid principal amount for EMI "
                                + emi.getEmiScheduleId()
                );
            }

            principalRepaid = principalRepaid.add(
                    emi.getPrincipalAmount()
            );
        }

        // 4. Amount must match exactly
        if (dto.getPaymentAmount().compareTo(requiredAmount) != 0) {
            throw new InvalidPaymentAmountException(
                    "Required amount is ₹" + requiredAmount
                            + ", received ₹" + dto.getPaymentAmount()
            );
        }



        // 5. Calculate outstanding principal
        BigDecimal outstanding = account.getOutstandingAmount() != null
                ? account.getOutstandingAmount()
                : BigDecimal.ZERO;

        BigDecimal newOutstanding = outstanding.subtract(principalRepaid);

        if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Principal repayment exceeds outstanding principal."
            );
        }

        LocalDateTime paidAt = LocalDateTime.now();

        // 6. Save one ledger record for this transaction
        LoanPayments payment = new LoanPayments();
        payment.setLoanAccountId(account.getLoanAccountId());
        payment.setPaymentAmount(requiredAmount);
        payment.setPaymentDate(paidAt);
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentName(
                dto.getPaymentName() != null
                        ? dto.getPaymentName()
                        : payableEmis.size() > 1
                        ? "Combined EMI Repayment"
                        : "EMI Repayment"
        );

        LoanPayments savedPayment = loanPaymentRepository.save(payment);

        // 7. Mark all included EMIs as paid
        for (EmiSchedules emi : payableEmis) {
            emi.setPaymentStatus("PAID");
            emi.setPaidDate(paidAt);
        }

        emiSchedulesRepo.saveAll(payableEmis);

        // 8. Update account totals
        BigDecimal totalPaid = account.getTotalPaidAmount() != null
                ? account.getTotalPaidAmount()
                : BigDecimal.ZERO;

        account.setTotalPaidAmount(totalPaid.add(requiredAmount));
        account.setOutstandingAmount(newOutstanding);

        boolean allPendingEmisPaid =
                pendingEmis.size() == payableEmis.size();

        if (allPendingEmisPaid
                && newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            account.setStatus(LoanAccountStatus.CLOSED);
        }

        loanAccountRepo.save(account);

        // 9. Notify customer
        if (account.getCustomerId() != null) {
            notificationService.sendNotification(
                    account.getCustomerId(),
                    "Payment of ₹" + requiredAmount
                            + " received for Loan Account "
                            + account.getLoanAccountNo()
                            + ". Payment ID: " + savedPayment.getPaymentId(),
                    "EMI Payment Confirmation"
            );
        }

        // 10. Response
        LoanPaymentResponseDTO response =
                modelMapper.map(savedPayment, LoanPaymentResponseDTO.class);

        response.setLoanAccountId(account.getLoanAccountId());
        response.setPaidAmount(savedPayment.getPaymentAmount());
        response.setUpdatedOutstandingPrincipal(account.getOutstandingAmount());
        response.setTotalPaidAmount(account.getTotalPaidAmount());
        response.setMessage(
                payableEmis.size() > 1
                        ? "All due EMIs paid successfully."
                        : "EMI paid successfully."
        );

        return response;
    }



    @Override
    public List<LoanPaymentResponseDTO> getAllPaymentsForAdmin() {
        List<LoanPayments> payments = loanPaymentRepository.findAll();
        return payments.stream()
                .map(p -> modelMapper.map(p, LoanPaymentResponseDTO.class)).toList();


    }

    @Transactional
    @Override
    public LoanPaymentResponseDTO processForeClosurePayment(
            ForeClosurePaymentRequestDto dto) {

        // 1. Fetch the foreclosure request
        ForeClosureRequest request = foreClosureRequestRepository
                .findById(dto.getForeClosureRequestId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Foreclosure request not found: "
                                        + dto.getForeClosureRequestId()));

        // 2. Validate approval and payment status
        if (!"APPROVED".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException(
                    "Foreclosure request is not approved");
        }

        if (Boolean.TRUE.equals(request.getIsPaid())) {
            throw new IllegalArgumentException(
                    "Foreclosure request is already paid");
        }

        // 3. Validate the amount against the approved request
        BigDecimal approvedAmount =
                "FULL".equalsIgnoreCase(request.getForeClosureType())
                        ? request.getForeClosureAmount()
                        : request.getPartialAmount();

        if (dto.getPaymentAmount() == null
                || approvedAmount == null
                || dto.getPaymentAmount().compareTo(approvedAmount) != 0) {
            throw new IllegalArgumentException(
                    "Payment amount does not match the approved foreclosure amount");
        }

        // IMPORTANT:
        // Call this method only AFTER actual Razorpay payment verification.
        // Do not trust payment IDs or signatures supplied by the frontend alone.

        // 4. Save the verified payment
        LoanPayments payment = new LoanPayments();
        payment.setLoanAccountId(request.getLoanAccountId());
        payment.setPaymentAmount(dto.getPaymentAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentName(
                "FULL".equalsIgnoreCase(request.getForeClosureType())
                        ? "Full Foreclosure Payment"
                        : "Partial Foreclosure Payment");

        LoanPayments savedPayment = loanPaymentRepository.save(payment);


//        doneClosure(
//                request.getRequestId(),
//                savedPayment.getPaymentAmount()
//        );


        LoanPaymentResponseDTO response =
                modelMapper.map(savedPayment, LoanPaymentResponseDTO.class);

        response.setMessage(
                "Foreclosure payment processed successfully");

        return response;
    }






}
