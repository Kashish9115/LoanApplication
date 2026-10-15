package com.example.loanApplication.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CibilScoreRangeReportDto {

    private String cibilScoreRange; // e.g., "750 - 900"
    private Long applicants;        // Total applicants in this range
    private Long approved;          // Approved applicants
    private Double approvalPercentage; // Approval rate (%)
}