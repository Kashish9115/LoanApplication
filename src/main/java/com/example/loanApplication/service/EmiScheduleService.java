package com.example.loanApplication.service;


import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.dto.PenaltyChargesRequestDto;
import com.example.loanApplication.entity.LoanAccount;

import java.math.BigDecimal;
import java.util.List;

public interface EmiScheduleService {

    List<EmiScheduleResponseDto> generateEmiScheduleByLoanAccountId(Integer loanAccountId);

    List<PenaltyChargesRequestDto> getItemizedEmiBreakdown(Long emiScheduleId);

    List<EmiScheduleResponseDto> getUserEmiSchedules(Integer loanAccountId);

    List<EmiScheduleResponseDto> getUpcomingEmi(Integer loanAccountId);

     EmiScheduleResponseDto  generateEmiSchedule(LoanAccount account);


  // void fillEmiOnDue();

 List<EmiScheduleResponseDto>   regenerateEmiSchedule(Integer loanAccountId, BigDecimal newPrincipal);
    //void cancelFutureEmis(Integer loanAccountId, String reason);

   // List<EmiScheduleResponseDto> recalculateEmiSchedule(Integer loanAccountId);
}



