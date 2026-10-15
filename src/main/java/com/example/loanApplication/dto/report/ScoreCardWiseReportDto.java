package com.example.loanApplication.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScoreCardWiseReportDto {

    private String scoreCard;          // "Excellent (A)", "Good (B)", "Average (C)", "Poor (D)"
    private Long applicants;           // Total applicants count in this risk category
    private Long approved;             // Approved count
    private Long rejected;             // Rejected count
    private Long pending;              // Pending count
    private Double approvalPercentage; // Approval rate %
}