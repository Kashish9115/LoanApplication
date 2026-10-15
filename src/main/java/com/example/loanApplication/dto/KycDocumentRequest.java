package com.example.loanApplication.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class KycDocumentRequest {
    @NotNull @Positive
    private Integer customerId;

    @NotBlank
    private String documentType;


    private String filePath;
}
