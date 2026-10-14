package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.InterestAccrualResponseDTO;
import com.example.loanApplication.dto.PenaltyChargesRequestDto;
import com.example.loanApplication.dto.PenaltyChargesResponseDto;
import com.example.loanApplication.entity.PenaltyCharges;
import com.example.loanApplication.repository.EmiSchedulesRepo;
import com.example.loanApplication.repository.InterestAccrualRepo;
import com.example.loanApplication.repository.LoanAccountRepository;
import com.example.loanApplication.repository.PenaltyChargesRepo;
import com.example.loanApplication.service.PenaltyService;
import jakarta.transaction.Transactional;
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
    public List<PenaltyChargesResponseDto> getPenaltiesByAccountId(Integer LoanAccountId) {
        List<PenaltyCharges>  penaltieList = penaltyChargesRepo.findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(LoanAccountId);

        return  penaltieList.stream().map(p->modelMapper.map(p , PenaltyChargesResponseDto.class)).toList();

    }





}
