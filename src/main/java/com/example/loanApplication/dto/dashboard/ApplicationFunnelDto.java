package com.example.loanApplication.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationFunnelDto {
    private Long submittedCount;
    private Long kycVerifiedCount;
    private Long reviewedCount;
    private Long approvedCount;
    private Long disbursedCount;

}
