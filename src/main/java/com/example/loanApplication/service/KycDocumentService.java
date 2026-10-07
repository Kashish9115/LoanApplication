package com.example.loanApplication.service;

import com.example.loanApplication.dto.KycDocumentRequest;
import com.example.loanApplication.dto.KycDocumentResponse;
import com.example.loanApplication.dto.KycVerificationRequest;

import java.util.List;

public interface KycDocumentService {

    KycDocumentResponse createDocument(KycDocumentRequest request);

    KycDocumentResponse getDocument(Integer documentId);

    List<KycDocumentResponse> getDocumentsByCustomer(Integer customerId);

    KycDocumentResponse updateDocument(
            Integer documentId,
            KycDocumentRequest request
    );

    KycDocumentResponse updateVerificationStatus(
            Integer documentId,
            KycVerificationRequest request
    );

    void deleteDocument(Integer documentId);
}
