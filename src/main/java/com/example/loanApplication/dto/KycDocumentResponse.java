package com.example.loanApplication.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KycDocumentResponse {
    private Integer documentId;
    private Integer customerId;
    private String documentType;
    private String filePath;
    private String originalFileName;
    private String contentType;
    private String verificationStatus;
    private String rejectionReason;
    private LocalDateTime reviewedAt;
}
