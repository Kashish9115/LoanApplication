package com.example.loanApplication.service;

import com.example.loanApplication.dto.PenaltyChargesRequestDto;

import java.math.BigDecimal;

public interface PenaltyService {




  PenaltyChargesRequestDto applyBounceCharge(Long emiScheduleId, BigDecimal bounceAmount);


  PenaltyChargesRequestDto calculateDailyLateInterest(Long loanAccountId, int daysOverdue);

   PenaltyChargesRequestDto waivePenaltyCharge(Long penaltyChargeId, Long ticketId);



    //processForeclosureRequest(ForeclosureDTO dto);

}
