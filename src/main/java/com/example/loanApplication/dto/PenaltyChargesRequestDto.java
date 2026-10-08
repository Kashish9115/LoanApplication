package com.example.loanApplication.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PenaltyChargesRequestDto {


    private Integer penaltyChargeId;
    private Integer emiScheduleId;
    private Integer loanAccountId;
    private BigDecimal penaltyAmount;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
