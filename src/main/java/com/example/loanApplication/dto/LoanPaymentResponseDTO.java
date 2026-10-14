package com.example.loanApplication.dto;

import com.example.loanApplication.enumeration.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPaymentResponseDTO {


    private Integer paymentId;
    private Integer loanAccountId;
    private Integer emiScheduleId;
    private BigDecimal paidAmount;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private BigDecimal updatedOutstandingPrincipal;
    private BigDecimal totalPaidAmount;
    private String message;

}
