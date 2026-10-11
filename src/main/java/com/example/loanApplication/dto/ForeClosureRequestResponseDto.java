package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForeClosureRequestResponseDto {

    private Integer requestId;
    private Integer loanAccountId;
    private String loanAccountNo;
    private String foreClosureType;
    private BigDecimal foreClosureAmount;
    private LocalDateTime requestedDate;
    private LocalDateTime expectedClosureDate;
    private String reason;
    private String status;
    private Boolean isPaid;
}