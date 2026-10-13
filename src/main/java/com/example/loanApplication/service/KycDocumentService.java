package com.example.loanApplication.service;

import com.example.loanApplication.dto.*;
import java.util.List;

public interface KycDocumentService {
    KycDocumentResponse uploadDocument(KycDocumentRequest request);
    List<KycDocumentResponse> getCustomerKyc(Integer customerId);
    List<KycDocumentResponse> getPendingDocuments();
    KycDocumentResponse getDocument(Integer documentId);
    KycDocumentResponse verifyDocument(Integer documentId, KycVerificationRequest request);
    boolean isKycCompleted(Integer customerId);
    void deleteDocument(Integer documentId);
}
