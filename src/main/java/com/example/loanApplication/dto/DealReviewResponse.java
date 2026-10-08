package com.example.loanApplication.dto;

import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.enumeration.ReviewStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealReviewResponse {

    private Integer reviewId;


    private Integer dealId;

    private Integer customerId;

    private Integer officerId;

    private ReviewStatus reviewStatus;

    private LoanDealStatus dealStatus;

    private BigDecimal requestedLoanAmount;

    private BigDecimal approvedAmount;

    private String message;
}