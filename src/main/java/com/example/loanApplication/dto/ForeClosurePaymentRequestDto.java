
package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForeClosurePaymentRequestDto {

    private Integer foreClosureRequestId;
    private BigDecimal paymentAmount;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
