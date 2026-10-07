package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.KycDocumentRequest;
import com.example.loanApplication.dto.KycDocumentResponse;
import com.example.loanApplication.dto.KycVerificationRequest;
import com.example.loanApplication.entity.KycDocument;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.KycDocumentRepository;
import com.example.loanApplication.service.KycDocumentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KycDocumentServiceImpl implements KycDocumentService {

    private final KycDocumentRepository repository;

    public KycDocumentServiceImpl(KycDocumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public KycDocumentResponse createDocument(KycDocumentRequest request) {

        KycDocument document = new KycDocument();

        document.setCustomerId(request.getCustomerId());
        System.out.println("Customer ID = " + request.getCustomerId());
        document.setDocumentType(request.getDocumentType());
        document.setFilePath(request.getFilePath());
        document.setVerificationStatus("PENDING");

        KycDocument saved = repository.save(document);

        return convertToResponse(saved);
    }

    @Override
    public KycDocumentResponse getDocument(Integer documentId) {

        KycDocument document = repository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "KYC document not found with id: " + documentId
                        )
                );

        return convertToResponse(document);
    }

    @Override
    public List<KycDocumentResponse> getDocumentsByCustomer(Integer customerId) {

        return repository.findByCustomerId(customerId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public KycDocumentResponse updateDocument(
            Integer documentId,
            KycDocumentRequest request
    ) {

        KycDocument document = repository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "KYC document not found with id: " + documentId
                        )
                );

        document.setCustomerId(request.getCustomerId());
        document.setDocumentType(request.getDocumentType());
        document.setFilePath(request.getFilePath());

        KycDocument updated = repository.save(document);

        return convertToResponse(updated);
    }

    @Override
    public KycDocumentResponse updateVerificationStatus(
            Integer documentId,
            KycVerificationRequest request
    ) {

        KycDocument document = repository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "KYC document not found with id: " + documentId
                        )
                );

        String status = request.getVerificationStatus();

        if (!status.equalsIgnoreCase("PENDING")
                && !status.equalsIgnoreCase("VERIFIED")
                && !status.equalsIgnoreCase("REJECTED")) {

            throw new IllegalArgumentException(
                    "Status must be PENDING, VERIFIED or REJECTED"
            );
        }

        document.setVerificationStatus(status.toUpperCase());

        KycDocument updated = repository.save(document);

        return convertToResponse(updated);
    }

    @Override
    public void deleteDocument(Integer documentId) {

        KycDocument document = repository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "KYC document not found with id: " + documentId
                        )
                );

        repository.delete(document);
    }

    private KycDocumentResponse convertToResponse(KycDocument document) {

        KycDocumentResponse response = new KycDocumentResponse();

        response.setDocumentId(document.getDocumentId());
        response.setCustomerId(document.getCustomerId());
        response.setDocumentType(document.getDocumentType());
        response.setFilePath(document.getFilePath());
        response.setVerificationStatus(document.getVerificationStatus());

        return response;
    }



}
