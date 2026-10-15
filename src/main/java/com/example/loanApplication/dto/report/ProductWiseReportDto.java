package com.example.loanApplication.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductWiseReportDto {

    private String product; // Formatted Enum Display Name (e.g. "Personal Loan")
    private Long totalApps; // Total applications count
    private Long approved; // Approved count
    private Long rejected; // Rejected count
    private Long disbursed; // Disbursed count
    private Double disbursementPercentage; // Disbursement rate %
}
