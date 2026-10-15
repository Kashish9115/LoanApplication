package com.example.loanApplication.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyTrendDto {

    private Integer monthNumber; // 1 to 12

    private String monthName; // "JANUARY", "FEBRUARY", etc.

    private Long totalApplications;
}