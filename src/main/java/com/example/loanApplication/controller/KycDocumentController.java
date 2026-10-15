
package com.example.loanApplication.controller;

import com.example.loanApplication.dto.KycBulkVerificationRequest;
import com.example.loanApplication.dto.KycDocumentRequest;
import com.example.loanApplication.dto.KycDocumentResponse;
import com.example.loanApplication.dto.KycVerificationRequest;
import com.example.loanApplication.service.KycDocumentService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/kyc")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://127.0.0.1:4200"
})
public class KycDocumentController {

    private final KycDocumentService service;

    public KycDocumentController(KycDocumentService service) {
        this.service = service;
    }

    @PostMapping(
            value = "/documents/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<KycDocumentResponse> uploadFile(
            @RequestParam Integer customerId,
            @RequestParam String documentType,
            @RequestParam("file") MultipartFile file) {

        log.info(
                "KYC upload requested: customerId={}, documentType={}",
                customerId,
                documentType
        );

        KycDocumentResponse response =
                service.uploadFile(customerId, documentType, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/documents")
    public ResponseEntity<KycDocumentResponse> upload(
            @Valid @RequestBody KycDocumentRequest request) {

        KycDocumentResponse response = service.uploadDocument(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<KycDocumentResponse>> customerKyc(
            @PathVariable Integer customerId) {

        return ResponseEntity.ok(service.getCustomerKyc(customerId));
    }

    @GetMapping("/customer/{customerId}/paged")
    public ResponseEntity<Page<KycDocumentResponse>> customerKycPaged(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = createPageable(page, size, "documentId");

        return ResponseEntity.ok(
                service.getCustomerKycPage(customerId, pageable)
        );
    }

    @GetMapping("/officer/pending")
    public ResponseEntity<List<KycDocumentResponse>> pending() {
        return ResponseEntity.ok(service.getPendingDocuments());
    }

    @GetMapping("/officer/pending/paged")
    public ResponseEntity<Page<KycDocumentResponse>> pendingPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = createPageable(page, size, "documentId");

        return ResponseEntity.ok(
                service.getPendingDocumentsPage(pageable)
        );
    }

    @GetMapping("/officer/documents")
    public ResponseEntity<List<KycDocumentResponse>> allForOfficer() {
        return ResponseEntity.ok(service.getAllDocuments());
    }

    @GetMapping("/officer/documents/paged")
    public ResponseEntity<Page<KycDocumentResponse>> allForOfficerPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = createPageable(page, size, "customerId");

        return ResponseEntity.ok(
                service.getAllDocumentsPage(pageable)
        );
    }

    @GetMapping("/documents/{documentId}")
    public ResponseEntity<KycDocumentResponse> getDocument(
            @PathVariable Integer documentId) {

        return ResponseEntity.ok(service.getDocument(documentId));
    }

    @GetMapping("/documents/{documentId}/file")
    public ResponseEntity<byte[]> getDocumentFile(
            @PathVariable Integer documentId) {

        byte[] file = service.readDocumentFile(documentId);

        MediaType contentType;

        try {
            contentType = MediaType.parseMediaType(
                    service.getDocumentContentType(documentId)
            );
        } catch (IllegalArgumentException ex) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        String fileName = service.getDocumentFileName(documentId)
                .replaceAll("[\\r\\n\"]", "_");

        ContentDisposition disposition = ContentDisposition
                .inline()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(file);
    }

    @PutMapping("/officer/documents/{documentId}/verification")
    public ResponseEntity<KycDocumentResponse> verify(
            @PathVariable Integer documentId,
            @Valid @RequestBody KycVerificationRequest request) {

        return ResponseEntity.ok(
                service.verifyDocument(documentId, request)
        );
    }

    @PutMapping("/officer/documents/bulk-verification")
    public ResponseEntity<List<KycDocumentResponse>> bulkVerify(
            @Valid @RequestBody KycBulkVerificationRequest request) {

        log.info(
                "Bulk KYC verification requested: documentCount={}",
                request.getDocumentIds() == null
                        ? 0
                        : request.getDocumentIds().size()
        );

        return ResponseEntity.ok(
                service.verifyDocumentsBulk(request)
        );
    }

    @GetMapping("/customer/{customerId}/status")
    public ResponseEntity<Boolean> status(
            @PathVariable Integer customerId) {

        return ResponseEntity.ok(
                service.isKycCompleted(customerId)
        );
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer documentId) {

        service.deleteDocument(documentId);

        return ResponseEntity.noContent().build();
    }

    private Pageable createPageable(int page, int size, String sortBy) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, sortBy)
        );
    }
}
