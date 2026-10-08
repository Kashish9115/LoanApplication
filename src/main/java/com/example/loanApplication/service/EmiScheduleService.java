package com.example.loanApplication.service;


import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.dto.PenaltyChargesDto;
import com.example.loanApplication.entity.LoanAccount;

import java.math.BigDecimal;
import java.util.List;

public interface EmiScheduleService {

    List<EmiScheduleResponseDto> generateEmiScheduleByLoanAccountId(Integer loanAccountId);

    List<PenaltyChargesDto> getItemizedEmiBreakdown(Long emiScheduleId);

    List<EmiScheduleResponseDto> getUserEmiSchedules(Integer loanAccountId);

    List<EmiScheduleResponseDto> getUpcomingEmi(Integer loanAccountId);

     EmiScheduleResponseDto  generateEmiSchedule(LoanAccount account);


  // void fillEmiOnDue();

 List<EmiScheduleResponseDto>   regenerateEmiSchedule(Long loanAccountId, BigDecimal newPrincipal)
    //void cancelFutureEmis(Integer loanAccountId, String reason);

   // List<EmiScheduleResponseDto> recalculateEmiSchedule(Integer loanAccountId);
}



