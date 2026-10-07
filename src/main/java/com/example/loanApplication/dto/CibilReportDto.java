package com.example.loanApplication.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CibilReportDto {

    private Integer cibilReportId;
    private Integer customerId;
    private String panNo;
    private Integer cibilScore;
    private LocalDateTime checkDate;
    private String status;
}
