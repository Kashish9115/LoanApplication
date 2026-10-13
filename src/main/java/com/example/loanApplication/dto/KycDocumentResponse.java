package com.example.loanApplication.dto;

import lombok.Data;

@Data
public class KycDocumentResponse {
    private Integer documentId;
    private Integer customerId;
    private String documentType;
    private String filePath;
    private String verificationStatus;
}
