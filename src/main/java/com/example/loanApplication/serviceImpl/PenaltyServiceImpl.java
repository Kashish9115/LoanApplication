package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.InterestAccrualResponseDTO;
import com.example.loanApplication.dto.PenaltyChargesRequestDto;
import com.example.loanApplication.dto.PenaltyChargesResponseDto;
import com.example.loanApplication.entity.EmiSchedules;
import com.example.loanApplication.entity.InterestAccruals;
import com.example.loanApplication.entity.LoanAccount;
import com.example.loanApplication.entity.PenaltyCharges;
import com.example.loanApplication.enumeration.AccrualStatus;
import com.example.loanApplication.enumeration.EmiStatus;
import com.example.loanApplication.enumeration.LoanAccountStatus;
import com.example.loanApplication.enumeration.PenaltyStatus;
import com.example.loanApplication.repository.EmiSchedulesRepo;
import com.example.loanApplication.repository.InterestAccrualRepo;
import com.example.loanApplication.repository.LoanAccountRepository;
import com.example.loanApplication.repository.PenaltyChargesRepo;
import com.example.loanApplication.service.PenaltyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PenaltyServiceImpl  implements PenaltyService {
   private  final PenaltyChargesRepo penaltyChargesRepo;
 private  final LoanAccountRepository loanAccountRepository;
   private  final EmiSchedulesRepo emiSchedulesRepo;
   private  final ModelMapper modelMapper;
   private  final InterestAccrualRepo interestAccrualRepo;


    @Override
    public PenaltyChargesResponseDto applyBounceCharge(Integer emiScheduleId, BigDecimal bounceAmount) {

   EmiSchedules emi = emiSchedulesRepo.findById(emiScheduleId).get();

        PenaltyCharges penaltyCharges= new PenaltyCharges();
        penaltyCharges.setLoanAccount(emi.getLoanAccount());
        penaltyCharges.setEmiSchedule(emi);
        penaltyCharges.setPenaltyAmount(bounceAmount);
        penaltyCharges.setReason(" Your EMI Bounces ");
        penaltyCharges.setStatus(String.valueOf(PenaltyStatus.APPLIED));

     PenaltyCharges savedPenalty = penaltyChargesRepo.save(penaltyCharges);


  return modelMapper.map(savedPenalty, PenaltyChargesResponseDto.class);

    }

    @Override
    public InterestAccrualResponseDTO calculateDailyLateInterest(Integer loanAccountId, int daysOverdue) {

        LoanAccount loanAccount= loanAccountRepository.findById(loanAccountId).get();

        BigDecimal dailyInterest= BigDecimal.ONE;

        BigDecimal accruedInterest=dailyInterest.plus().add(new BigDecimal(daysOverdue));

     List<EmiSchedules> pendingInstallments=emiSchedulesRepo.findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(loanAccountId, String.valueOf(EmiStatus.PENDING));


     Integer installmentNo =pendingInstallments.isEmpty() ?1 : pendingInstallments.get(0).getInstallmentNo();
        InterestAccruals interestAccruals= new InterestAccruals();
     interestAccruals.setLoanAccountId(loanAccountId);
     interestAccruals.setInterestAmount(accruedInterest);
     interestAccruals.setAccrualDate(LocalDateTime.now());
     interestAccruals.setInstallmentNo(installmentNo);
     interestAccruals.setStatus(AccrualStatus.APPLIED.name());

   InterestAccruals saved =  interestAccrualRepo.save(interestAccruals);

        return modelMapper.map(saved, InterestAccrualResponseDTO.class);
    }

    @Override
    public PenaltyChargesRequestDto waivePenaltyCharge(Integer penaltyChargeId, Long ticketId) {
        return null;
    }

    @Override
    public List<PenaltyChargesResponseDto> getPenaltiesByAccountId(Integer LoanAccountId) {
        List<PenaltyCharges>  penaltieList = penaltyChargesRepo.findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(LoanAccountId);

        return  penaltieList.stream().map(p->modelMapper.map(p , PenaltyChargesResponseDto.class)).toList();

    }




//    @Override
//    @Transactional
//    public PenaltyChargeResponseDto waivePenaltyCharge(Long penaltyChargeId, Long ticketId) {
//        SupportTickets ticket = supportTicketRepository.findById(ticketId)
//                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found: " + ticketId));
//
//        PenaltyCharges penalty = penaltyChargeRepository.findById(penaltyChargeId)
//                .orElseThrow(() -> new ResourceNotFoundException("Penalty charge not found: " + penaltyChargeId));
//
//        penalty.setStatus("WAIVED");
//        penalty.setReason(penalty.getReason() + " [WAIVED via Ticket #" + ticketId + "]");
//        PenaltyCharges saved = penaltyChargeRepository.save(penalty);
//
//        ticket.setStatus("RESOLVED");
//        ticket.setUpdatedAt(LocalDateTime.now());
//        supportTicketRepository.save(ticket);
//
//        return modelMapper.map(saved, PenaltyChargeResponseDto.class);
//    }
//
}
