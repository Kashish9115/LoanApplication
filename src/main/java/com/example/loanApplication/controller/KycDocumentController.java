package com.example.loanApplication.controller;

import com.example.loanApplication.dto.KycDocumentRequest;
import com.example.loanApplication.dto.KycDocumentResponse;
import com.example.loanApplication.dto.KycVerificationRequest;
import com.example.loanApplication.service.KycDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kyc/documents")
public class KycDocumentController {

    private final KycDocumentService service;

    public KycDocumentController(KycDocumentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<KycDocumentResponse> createDocument(
            @RequestBody KycDocumentRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createDocument(request));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<KycDocumentResponse> getDocument(
            @PathVariable Integer documentId
    ) {

        return ResponseEntity.ok(
                service.getDocument(documentId)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<KycDocumentResponse>> getDocumentsByCustomer(
            @PathVariable Integer customerId
    ) {

        return ResponseEntity.ok(
                service.getDocumentsByCustomer(customerId)
        );
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<KycDocumentResponse> updateDocument(
            @PathVariable Integer documentId,
            @RequestBody KycDocumentRequest request
    ) {

        return ResponseEntity.ok(
                service.updateDocument(documentId, request)
        );
    }

    @PutMapping("/{documentId}/verification")
    public ResponseEntity<KycDocumentResponse> updateVerificationStatus(
            @PathVariable Integer documentId,
            @RequestBody KycVerificationRequest request
    ) {

        return ResponseEntity.ok(
                service.updateVerificationStatus(documentId, request)
        );
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Integer documentId
    ) {

        service.deleteDocument(documentId);

        return ResponseEntity.noContent().build();
    }
}