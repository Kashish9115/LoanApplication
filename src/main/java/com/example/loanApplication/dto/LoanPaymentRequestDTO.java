package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPaymentRequestDTO {

    private Long loanAccountId;
    private Long emiScheduleId;
    private BigDecimal paymentAmount;
    private String paymentName;        // e.g., "Monthly EMI", "Part-closure Prepayment"
    private String paymentMethod;      // e.g., "RAZORPAY"
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;


}
