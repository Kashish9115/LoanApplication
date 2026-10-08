package com.example.loanApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KycDocumentRequest {
    @NotNull
    private Integer customerId;
    @NotBlank
    private String documentType;
    @NotBlank
    private String filePath;
}
