package com.example.loanApplication.service;

import com.example.loanApplication.dto.PenaltyChargesDto;

import java.math.BigDecimal;

public interface PenaltyService {




  PenaltyChargesDto  applyBounceCharge(Long emiScheduleId, BigDecimal bounceAmount);


  PenaltyChargesDto calculateDailyLateInterest(Long loanAccountId, int daysOverdue);

   PenaltyChargesDto waivePenaltyCharge(Long penaltyChargeId, Long ticketId);



    //processForeclosureRequest(ForeclosureDTO dto);

}
