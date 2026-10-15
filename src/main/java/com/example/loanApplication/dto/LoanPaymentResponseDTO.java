package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPaymentResponseDTO {


    private Long paymentId;            // Generated primary key from LoanPayments table [1]
    private Long loanAccountId;
    private Long emiScheduleId;
    private BigDecimal paidAmount;
    private String paymentStatus;      // e.g., "SUCCESS", "FAILED"
    private LocalDateTime paymentDate;
    private BigDecimal updatedOutstandingPrincipal;
    private BigDecimal totalPaidAmount;
    private String message;

}
