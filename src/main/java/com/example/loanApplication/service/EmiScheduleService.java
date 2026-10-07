package com.example.loanApplication.service;


import com.example.loanApplication.dto.EmiScheduleResponseDto;

import java.util.List;

public interface EmiScheduleService {

    List<EmiScheduleResponseDto> generateEmi(Integer loanAccountId);


    List<EmiScheduleResponseDto> getUserEmiSchedules(Integer loanAccountId);

    List<EmiScheduleResponseDto> getUpcomingEmi(Integer loanAccountId);

   void fillEmiOnDue();

}
