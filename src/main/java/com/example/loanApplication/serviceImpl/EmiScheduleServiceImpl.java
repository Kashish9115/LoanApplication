package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.dto.PenaltyChargesDto;
import com.example.loanApplication.entity.LoanAccount;
//import com.example.loanApplication.service.EmailService;
import com.example.loanApplication.service.EmiScheduleService;

import java.math.BigDecimal;
import java.util.List;

public  class EmiScheduleServiceImpl  implements EmiScheduleService {


    @Override
    public List<EmiScheduleResponseDto> generateEmiScheduleByLoanAccountId(Integer loanAccountId) {
        return List.of();
    }

    @Override
    public List<PenaltyChargesDto> getItemizedEmiBreakdown(Long emiScheduleId) {
        return List.of();
    }

    @Override
    public List<EmiScheduleResponseDto> getUserEmiSchedules(Integer loanAccountId) {
        return List.of();
    }

    @Override
    public List<EmiScheduleResponseDto> getUpcomingEmi(Integer loanAccountId) {
        return List.of();
    }

    @Override
    public EmiScheduleResponseDto generateEmiSchedule(LoanAccount account) {
        return null;
    }

    @Override
    public List<EmiScheduleResponseDto> regenerateEmiSchedule(Long loanAccountId, BigDecimal newPrincipal) {
        return List.of();
    }
}