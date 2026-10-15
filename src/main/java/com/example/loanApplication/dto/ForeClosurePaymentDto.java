package com.example.loanApplication.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForeClosurePaymentDto {

    private Integer foreClosureRequestId;
    private Integer loanAccountId;
    private String loanAccountNo;

    private String foreClosureType;
    private BigDecimal foreClosureAmount;
    private BigDecimal outstandingAmount;

    private LocalDateTime expectedClosureDate;
    private String status;
    private Boolean isPaid;
    private String reason;
}

