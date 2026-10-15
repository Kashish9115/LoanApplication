package com.example.loanApplication.service;
import com.example.loanApplication.dto.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface KycDocumentService {
 KycDocumentResponse uploadDocument(KycDocumentRequest request);
 KycDocumentResponse uploadFile(Integer customerId, String documentType, MultipartFile file);
 List<KycDocumentResponse> getCustomerKyc(Integer customerId);
 List<KycDocumentResponse> getPendingDocuments();
 List<KycDocumentResponse> getAllDocuments();
 Page<KycDocumentResponse> getCustomerKycPage(Integer customerId, Pageable pageable);
 Page<KycDocumentResponse> getPendingDocumentsPage(Pageable pageable);
 Page<KycDocumentResponse> getAllDocumentsPage(Pageable pageable);
 KycDocumentResponse getDocument(Integer documentId);
 byte[] readDocumentFile(Integer documentId);
 String getDocumentContentType(Integer documentId);
 String getDocumentFileName(Integer documentId);
 KycDocumentResponse verifyDocument(Integer documentId, KycVerificationRequest request);
 List<KycDocumentResponse> verifyDocumentsBulk(KycBulkVerificationRequest request);
 boolean isKycCompleted(Integer customerId);
 void deleteDocument(Integer documentId);
}
