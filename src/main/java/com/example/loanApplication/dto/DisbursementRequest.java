
package com.example.loanApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisbursementRequest {

    @NotNull(message = "Deal ID is required")
    private Integer dealId;

    @NotNull(message = "Disbursement date is required")
    private LocalDate disbursementDate;

    @NotBlank(message = "Bank partner is required")
    private String bankName;
}
