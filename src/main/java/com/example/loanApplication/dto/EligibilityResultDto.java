package com.example.loanApplication.dto;

import com.example.loanApplication.enumeration.EligibilityStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityResultDto {

    private Integer eligibilityId;

    private Integer customerId;

    private String customerName;

    private String panNo;

    private BigDecimal monthlyIncome;

    private Integer cibilScore;

    private Boolean isEligible;

    private BigDecimal loanAmount;

    private String rejectionReason;

    private EligibilityStatusEnum status;
}