package com.example.loanApplication.service;

import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.dto.PenaltyChargesResponseDto;
import com.example.loanApplication.entity.EmiSchedules;

import java.time.LocalDate;


public interface PenaltyService {



    PageResponse<PenaltyChargesResponseDto> getAllPenaltiesForAdmin(
            int page, int size);


    void applyBounceCharge(EmiSchedules schedule);

    //processForeclosureRequest(ForeclosureDTO dto);


    PageResponse<PenaltyChargesResponseDto> getPenaltiesByAccountId(
            Integer loanAccountId, int page, int size);




    void processOverdueEmi(EmiSchedules schedule, LocalDate today);


}
