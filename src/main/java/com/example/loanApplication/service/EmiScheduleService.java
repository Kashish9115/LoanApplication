package com.example.loanApplication.service;


import com.example.loanApplication.dto.EmiBreakDownDto;
import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.dto.OverdueEmiPaymentBreakdownDto;
import com.example.loanApplication.dto.PenaltyChargesRequestDto;
import com.example.loanApplication.entity.LoanAccount;

import java.math.BigDecimal;
import java.util.List;

public interface EmiScheduleService {


    List<EmiScheduleResponseDto> generateInitialSchedule(Integer loanAccountId);



   EmiBreakDownDto getEmiBreakdown(Integer emiScheduleId) ;



    List<EmiScheduleResponseDto>    regenerateScheduleOnPartialForeclosure(Integer loanAccountId, BigDecimal partialAmount );




    OverdueEmiPaymentBreakdownDto getAllOverdueEmiBreakdown(Integer loanAccountId);

}



