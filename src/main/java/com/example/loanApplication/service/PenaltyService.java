package com.example.loanApplication.service;

import com.example.loanApplication.dto.InterestAccrualResponseDTO;
import com.example.loanApplication.dto.PenaltyChargesRequestDto;
import com.example.loanApplication.dto.PenaltyChargesResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface PenaltyService {






   List<PenaltyChargesResponseDto> getPenaltiesByAccountId(Integer LoanAccountId);

    //processForeclosureRequest(ForeclosureDTO dto);

}
