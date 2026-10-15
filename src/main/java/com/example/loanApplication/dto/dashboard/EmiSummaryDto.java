package com.example.loanApplication.dto.dashboard;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmiSummaryDto {

    private Integer totalEmis;
    private Integer paidEmisCount;
    private BigDecimal paidEmisAmount;
    private Integer upcomingEmisCount;
    private BigDecimal upcomingEmiAmount;
    private LocalDate nextEmiDueDate;
    private Integer remainingEmisCount;
    private BigDecimal remainingAmount;
}
