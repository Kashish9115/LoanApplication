package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.LoanPaymentRequestDTO;
import com.example.loanApplication.dto.LoanPaymentResponseDTO;
import com.example.loanApplication.entity.EmiSchedules;
import com.example.loanApplication.entity.LoanAccount;
import com.example.loanApplication.entity.LoanPayments;
import com.example.loanApplication.entity.Notifications;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.EmiSchedulesRepo;
import com.example.loanApplication.repository.LoanAccountRepository;
import com.example.loanApplication.repository.LoanPaymentRepository;
import com.example.loanApplication.service.LoanPaymentService;
import com.example.loanApplication.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LoanPaymentServiceImpl implements LoanPaymentService {

    private final LoanPaymentRepository loanPaymentRepository;
    private  final LoanAccountRepository loanAccountRepository;
    private  final EmiSchedulesRepo emiSchedulesRepo;

    private  final NotificationService notificationService;

private  final ModelMapper modelMapper;
    @Override
    public List<LoanPaymentResponseDTO> getPaymentHistoryByAccountId(Integer LoanAccountId) {

        List<LoanPayments> loanpayments = loanPaymentRepository.findByLoanAccountIdOrderByPaymentDateDesc(LoanAccountId);


        return  loanpayments.stream().map(paymentHistory->modelMapper.map(paymentHistory, LoanPaymentResponseDTO.class)).toList();
    }

    @Override
    public  LoanPaymentResponseDTO processPayment(LoanPaymentRequestDTO dto)  {
  //  Optional<LoanAccount> loanAccount =    loanPaymentRepository.findById(loanPaymentRequestDTO.getLoanAccountId()).orElseThrow(()->new RuntimeException( "Account not found "));


        // 1. Fetch Loan Account
        LoanAccount account = loanAccountRepository.findById(dto.getLoanAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan Account not found: " + dto.getLoanAccountId()));

        // 2. Insert transaction entry in LoanPayments table
        LoanPayments payment = new LoanPayments();
        payment.setLoanAccountId(dto.getLoanAccountId());
       // payment.setLoanAccount(account);
        payment.setPaymentAmount(dto.getPaymentAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentName(dto.getPaymentName() != null ? dto.getPaymentName() : "EMI Repayment");

        LoanPayments savedPayment = loanPaymentRepository.save(payment);

        // 3. Mark EmiSchedules row as PAID
        if (dto.getEmiScheduleId() != null) {
            EmiSchedules schedule = emiSchedulesRepo.findById(dto.getEmiScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("EMI Schedule not found: " + dto.getEmiScheduleId()));

            schedule.setPaymentStatus("PAID");
            schedule.setPaidDate(LocalDateTime.now());
            emiSchedulesRepo.save(schedule);
        }

        // 4. Update LoanAccounts balance figures
        BigDecimal currentPaid = account.getTotalPaidAmount() != null ? account.getTotalPaidAmount() : BigDecimal.ZERO;
        BigDecimal newTotalPaid = currentPaid.add(dto.getPaymentAmount());

        BigDecimal currentPrincipal = account.getOutstandingAmount() != null ? account.getOutstandingAmount() : BigDecimal.ZERO;
        BigDecimal newPrincipal = currentPrincipal.subtract(dto.getPaymentAmount());

        account.setTotalPaidAmount(newTotalPaid);
        account.setOutstandingAmount(newPrincipal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newPrincipal);
        loanAccountRepository.save(account);

        // 5. Trigger Async Notification Email & In-App Alert
        if (account.getCustomerId() != null) {
            notificationService.sendNotification(
                    account.getCustomerId(),
                    "Payment of ₹" + dto.getPaymentAmount() + " received successfully. Payment Ref ID: " + savedPayment.getPaymentId(),
                    "Payment Confirmation Receipt"
            );
        }

        // 6. Map to DTO using ModelMapper
        LoanPaymentResponseDTO response = modelMapper.map(savedPayment, LoanPaymentResponseDTO.class);
        response.setUpdatedOutstandingPrincipal(account.getOutstandingAmount());
        response.setTotalPaidAmount(account.getTotalPaidAmount());
        response.setMessage("Payment processed and database ledger updated successfully.");
        return response;


    }
}
