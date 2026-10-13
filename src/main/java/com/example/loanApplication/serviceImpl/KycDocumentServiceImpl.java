package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.*;
import com.example.loanApplication.entity.KycDocument;
import com.example.loanApplication.repository.KycDocumentRepository;
import com.example.loanApplication.service.KycDocumentService;
import com.example.loanApplication.exception.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class KycDocumentServiceImpl implements KycDocumentService {
    private static final Set<String> REQUIRED = Set.of("PAN", "AADHAAR", "SALARY_SLIP");

    private final KycDocumentRepository repository;
    private final ModelMapper modelMapper;

    public KycDocumentServiceImpl(KycDocumentRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public KycDocumentResponse uploadDocument(KycDocumentRequest request) {
        String type = normalize(request.getDocumentType());
        if (!REQUIRED.contains(type)) {
            throw new IllegalArgumentException("Document type must be PAN, AADHAAR or SALARY_SLIP");
        }
        if (repository.countByCustomerAndType(request.getCustomerId(), type) > 0) {
            throw new IllegalArgumentException(type + " document is already uploaded for this customer");
        }
        KycDocument document = modelMapper.map(request, KycDocument.class);
        document.setDocumentType(type);
        document.setVerificationStatus("PENDING");
        return response(repository.save(document));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getCustomerKyc(Integer customerId) {
        return repository.findByCustomerId(customerId).stream().map(this::response).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getPendingDocuments() {
        return repository.findByStatus("PENDING").stream().map(this::response).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KycDocumentResponse getDocument(Integer documentId) {
        return response(required(documentId));
    }

    @Override
    public KycDocumentResponse verifyDocument(Integer documentId, KycVerificationRequest request) {
        String status = request.getVerificationStatus();
        if (status == null || (!status.equalsIgnoreCase("PENDING")
                && !status.equalsIgnoreCase("VERIFIED")
                && !status.equalsIgnoreCase("REJECTED"))) {
            throw new IllegalArgumentException("Status must be PENDING, VERIFIED or REJECTED");
        }
        KycDocument document = required(documentId);
        document.setVerificationStatus(status.toUpperCase());
        return response(repository.save(document));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isKycCompleted(Integer customerId) {
        List<KycDocument> docs = repository.findByCustomerId(customerId);
        if (docs.size() != 3) return false;
        boolean pan = false, aadhaar = false, salary = false;
        for (KycDocument d : docs) {
            if ("PAN".equalsIgnoreCase(d.getDocumentType()) && "VERIFIED".equalsIgnoreCase(d.getVerificationStatus())) pan = true;
            if ("AADHAAR".equalsIgnoreCase(d.getDocumentType()) && "VERIFIED".equalsIgnoreCase(d.getVerificationStatus())) aadhaar = true;
            if ("SALARY_SLIP".equalsIgnoreCase(d.getDocumentType()) && "VERIFIED".equalsIgnoreCase(d.getVerificationStatus())) salary = true;
        }
        return pan && aadhaar && salary;
    }

    @Override
    public void deleteDocument(Integer documentId) {
        repository.delete(required(documentId));
    }

    private KycDocument required(Integer id) {
        KycDocument d = repository.findById(id);
        if (d == null) throw new ResourceNotFoundException("KYC document not found with id: " + id);
        return d;
    }

    private String normalize(String value) {
        if (value == null) throw new IllegalArgumentException("Document type is required");
        return value.trim().toUpperCase();
    }

    private KycDocumentResponse response(KycDocument d) {
        return modelMapper.map(d, KycDocumentResponse.class);
    }
}
