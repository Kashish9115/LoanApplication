package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayOrderResponseDto {




    private String razorpayOrderId;

    private String currency;
    private BigDecimal amount;

    private Integer emiScheduleId;
    private Integer loanAccountId;
}
