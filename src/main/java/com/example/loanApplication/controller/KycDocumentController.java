package com.example.loanApplication.controller;

import com.example.loanApplication.dto.*;
import com.example.loanApplication.service.KycDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/kyc")
public class KycDocumentController {
    private final KycDocumentService service;

    public KycDocumentController(KycDocumentService service) {
        this.service = service;
    }

    @PostMapping("/documents")
    public ResponseEntity<KycDocumentResponse> upload(@Valid @RequestBody KycDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.uploadDocument(request));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<KycDocumentResponse>> customerKyc(@PathVariable Integer customerId) {
        return ResponseEntity.ok(service.getCustomerKyc(customerId));
    }

    @GetMapping("/officer/pending")
    public ResponseEntity<List<KycDocumentResponse>> pending() {
        return ResponseEntity.ok(service.getPendingDocuments());
    }

    @GetMapping("/documents/{documentId}")
    public ResponseEntity<KycDocumentResponse> document(@PathVariable Integer documentId) {
        return ResponseEntity.ok(service.getDocument(documentId));
    }

    @PutMapping("/officer/documents/{documentId}/verification")
    public ResponseEntity<KycDocumentResponse> verify(
            @PathVariable Integer documentId,
            @Valid @RequestBody KycVerificationRequest request) {
        return ResponseEntity.ok(service.verifyDocument(documentId, request));
    }

    @GetMapping("/customer/{customerId}/status")
    public ResponseEntity<Boolean> status(@PathVariable Integer customerId) {
        return ResponseEntity.ok(service.isKycCompleted(customerId));
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> delete(@PathVariable Integer documentId) {
        service.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
