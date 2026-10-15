package com.example.loanApplication.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TurnAroundTimeReportDto {

    private String stage;       // "Application to Review", "Review to Approval", "Approval to Disbursement", "Total TAT"
    private Double avgTatDays;  // Average turnaround time in days
    private Double minDays;     // Minimum days taken
    private Double maxDays;     // Maximum days taken
}