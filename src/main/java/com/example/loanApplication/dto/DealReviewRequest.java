package com.example.loanApplication.dto;

import com.example.loanApplication.enumeration.ReviewStatus;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealReviewRequest {

    @NotNull(message = "Deal ID is required")
    private Integer dealId;


    @NotNull(message = "Officer ID is required")
    private Integer officerId;

    @NotNull(message = "Review status is required")
    private ReviewStatus status;
}