package com.example.loanApplication.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionEfficiencyDto {

    private Integer monthNumber;

    private BigDecimal billedAmount;

    private BigDecimal collectedAmount;

    private Double efficiencyPercentage;
}