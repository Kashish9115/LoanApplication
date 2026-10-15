
package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.KycBulkVerificationRequest;
import com.example.loanApplication.dto.KycDocumentRequest;
import com.example.loanApplication.dto.KycDocumentResponse;
import com.example.loanApplication.dto.KycVerificationRequest;
import com.example.loanApplication.entity.KycDocument;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.KycDocumentDetailsRepository;
import com.example.loanApplication.repository.KycDocumentRepository;
import com.example.loanApplication.service.KycDocumentService;

import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class KycDocumentServiceImpl implements KycDocumentService {

    private static final Set<String> REQUIRED_DOCUMENT_TYPES =
            Set.of("PAN", "AADHAAR", "SALARY_SLIP");

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("application/pdf", "image/jpeg", "image/png");

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("PENDING", "VERIFIED", "REJECTED");

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final KycDocumentRepository repository;
    private final ModelMapper modelMapper;
    private final KycDocumentDetailsRepository detailsRepository;
    private final Path uploadDirectory;

    public KycDocumentServiceImpl(
            KycDocumentRepository repository,
            ModelMapper modelMapper,
            KycDocumentDetailsRepository detailsRepository,
            @Value("${app.upload-dir:uploads/kyc}") String uploadDir) {

        this.repository = repository;
        this.modelMapper = modelMapper;
        this.detailsRepository = detailsRepository;
        this.uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public KycDocumentResponse uploadDocument(KycDocumentRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "KYC document request is required"
            );
        }

        String documentType = normalizeDocumentType(
                request.getDocumentType()
        );

        validateDocumentType(documentType);

        if (request.getCustomerId() == null
                || request.getCustomerId() <= 0) {
            throw new IllegalArgumentException(
                    "A valid customerId is required"
            );
        }

        if (request.getFilePath() != null
                && !request.getFilePath().isBlank()) {
            throw new IllegalArgumentException(
                    "File paths cannot be supplied by clients. "
                            + "Use the multipart upload endpoint"
            );
        }

        KycDocument document = modelMapper.map(
                request,
                KycDocument.class
        );

        document.setDocumentType(documentType);
        document.setVerificationStatus("PENDING");

        KycDocument savedDocument = repository.save(document);

        log.info(
                "KYC document created: documentId={}, customerId={}, type={}",
                savedDocument.getDocumentId(),
                savedDocument.getCustomerId(),
                documentType
        );

        return toResponse(savedDocument);
    }

    @Override
    public KycDocumentResponse uploadFile(
            Integer customerId,
            String documentType,
            MultipartFile file) {

        validateCustomerId(customerId);

        String normalizedType = normalizeDocumentType(documentType);
        validateDocumentType(normalizedType);
        validateFile(file);

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException(
                    "File content type is required"
            );
        }

        contentType = contentType.toLowerCase().trim();

        validateFileSignature(file, contentType);
        validateFileSignature(file, contentType);

        String originalFileName = getOriginalFileName(file);

        String extension = getFileExtension(originalFileName);

        String storedFileName = customerId
                + "_"
                + normalizedType.toLowerCase()
                + "_"
                + UUID.randomUUID()
                + extension;

        try {
            Files.createDirectories(uploadDirectory);

            Path target = uploadDirectory
                    .resolve(storedFileName)
                    .normalize();

            if (!target.startsWith(uploadDirectory)) {
                throw new IllegalArgumentException(
                        "Invalid file name"
                );
            }

            file.transferTo(target);

            KycDocument document = new KycDocument();
            document.setCustomerId(customerId);
            document.setDocumentType(normalizedType);
            document.setFilePath(target.toString());
            document.setVerificationStatus("PENDING");

            KycDocument savedDocument = repository.save(document);

            detailsRepository.save(
                    savedDocument.getDocumentId(),
                    originalFileName,
                    contentType,
                    null,
                    null
            );

            log.info(
                    "KYC upload completed: documentId={}, customerId={}, type={}",
                    savedDocument.getDocumentId(),
                    customerId,
                    normalizedType
            );

            return toResponse(savedDocument);

        } catch (IOException ex) {
            log.error(
                    "KYC file upload failed: customerId={}, type={}",
                    customerId,
                    normalizedType,
                    ex
            );

            throw new IllegalStateException(
                    "Could not save uploaded document",
                    ex
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getCustomerKyc(Integer customerId) {

        return repository
                .findByCustomerIdOrderByDocumentIdAsc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getPendingDocuments() {

        return repository
                .findByVerificationStatusIgnoreCaseOrderByCustomerIdAscDocumentIdAsc(
                        "PENDING"
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getAllDocuments() {

        return repository
                .findAllByOrderByCustomerIdAscDocumentIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KycDocumentResponse> getCustomerKycPage(
            Integer customerId,
            Pageable pageable) {

        return repository
                .findByCustomerId(customerId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KycDocumentResponse> getPendingDocumentsPage(
            Pageable pageable) {

        return repository
                .findByVerificationStatusIgnoreCase("PENDING", pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KycDocumentResponse> getAllDocumentsPage(
            Pageable pageable) {

        return repository
                .findAllByOrderByCustomerIdAscDocumentIdAsc(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public KycDocumentResponse getDocument(Integer documentId) {

        return toResponse(findDocument(documentId));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] readDocumentFile(Integer documentId) {

        KycDocument document = findDocument(documentId);

        if (document.getFilePath() == null
                || document.getFilePath().isBlank()) {
            throw new ResourceNotFoundException(
                    "No physical file is stored for this document"
            );
        }

        try {
            Path storedPath = Paths.get(document.getFilePath())
                    .toAbsolutePath()
                    .normalize();

            if (!storedPath.startsWith(uploadDirectory)) {
                log.warn(
                        "Rejected file path outside upload directory: documentId={}",
                        documentId
                );

                throw new ResourceNotFoundException(
                        "Document file is not available in private storage"
                );
            }

            return Files.readAllBytes(storedPath);

        } catch (InvalidPathException ex) {
            throw new ResourceNotFoundException(
                    "Document file path is invalid"
            );
        } catch (IOException ex) {
            throw new ResourceNotFoundException(
                    "Document file is missing from server storage"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getDocumentContentType(Integer documentId) {

        KycDocument document = findDocument(documentId);

        return detailsRepository.find(documentId)
                .map(KycDocumentDetailsRepository.Details::contentType)
                .filter(value -> !value.isBlank())
                .orElseGet(() -> getContentTypeFromPath(
                        document.getFilePath()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public String getDocumentFileName(Integer documentId) {

        KycDocument document = findDocument(documentId);

        return detailsRepository.find(documentId)
                .map(KycDocumentDetailsRepository.Details::originalFileName)
                .filter(value -> !value.isBlank())
                .orElseGet(() -> {
                    if (document.getFilePath() == null
                            || document.getFilePath().isBlank()) {
                        return "document-" + documentId;
                    }

                    try {
                        return Paths.get(document.getFilePath())
                                .getFileName()
                                .toString();
                    } catch (InvalidPathException ex) {
                        return "document-" + documentId;
                    }
                });
    }

    @Override
    public KycDocumentResponse verifyDocument(
            Integer documentId,
            KycVerificationRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Verification request is required"
            );
        }

        String status = normalizeStatus(
                request.getVerificationStatus()
        );

        validateVerificationStatus(status);

        KycDocument document = findDocument(documentId);

        validateRejectionReason(
                status,
                request.getRejectionReason()
        );

        document.setVerificationStatus(status);

        LocalDateTime reviewedAt = "PENDING".equals(status)
                ? null
                : LocalDateTime.now();

        KycDocument savedDocument = repository.save(document);

        String rejectionReason = "REJECTED".equals(status)
                ? request.getRejectionReason().trim()
                : null;

        detailsRepository.save(
                documentId,
                null,
                null,
                rejectionReason,
                reviewedAt
        );

        log.info(
                "KYC verification updated: documentId={}, customerId={}, status={}",
                documentId,
                savedDocument.getCustomerId(),
                status
        );

        return toResponse(savedDocument);
    }

    @Override
    public List<KycDocumentResponse> verifyDocumentsBulk(
            KycBulkVerificationRequest request) {

        if (request == null
                || request.getDocumentIds() == null
                || request.getDocumentIds().isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one document ID is required"
            );
        }

        String status = normalizeStatus(
                request.getVerificationStatus()
        );

        validateVerificationStatus(status);

        validateRejectionReason(
                status,
                request.getRejectionReason()
        );

        List<Integer> documentIds = new ArrayList<>(
                new LinkedHashSet<>(request.getDocumentIds())
        );

        if (documentIds.size() > 500) {
            throw new IllegalArgumentException(
                    "A maximum of 500 documents can be processed at once"
            );
        }

        if (documentIds.stream().anyMatch(
                id -> id == null || id <= 0)) {
            throw new IllegalArgumentException(
                    "Every document ID must be a positive integer"
            );
        }

        List<KycDocument> documents =
                repository.findAllById(documentIds);

        if (documents.size() != documentIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more selected KYC documents do not exist. "
                            + "No documents were updated"
            );
        }

        LocalDateTime reviewedAt = "PENDING".equals(status)
                ? null
                : LocalDateTime.now();

        String rejectionReason = "REJECTED".equals(status)
                ? request.getRejectionReason().trim()
                : null;

        for (KycDocument document : documents) {
            document.setVerificationStatus(status);
        }

        List<KycDocument> savedDocuments =
                repository.saveAll(documents);

        List<KycDocumentResponse> responses = new ArrayList<>();

        for (KycDocument document : savedDocuments) {
            detailsRepository.save(
                    document.getDocumentId(),
                    null,
                    null,
                    rejectionReason,
                    reviewedAt
            );

            responses.add(toResponse(document));
        }

        log.info(
                "Bulk KYC verification completed: count={}, status={}",
                responses.size(),
                status
        );

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isKycCompleted(Integer customerId) {

        List<KycDocument> documents =
                repository.findByCustomerIdOrderByDocumentIdAsc(
                        customerId
                );

        boolean panVerified = false;
        boolean aadhaarVerified = false;
        boolean salarySlipVerified = false;

        for (KycDocument document : documents) {

            if ("PAN".equalsIgnoreCase(document.getDocumentType())
                    && "VERIFIED".equalsIgnoreCase(
                    document.getVerificationStatus())) {
                panVerified = true;
            }

            if ("AADHAAR".equalsIgnoreCase(
                    document.getDocumentType())
                    && "VERIFIED".equalsIgnoreCase(
                    document.getVerificationStatus())) {
                aadhaarVerified = true;
            }

            if ("SALARY_SLIP".equalsIgnoreCase(
                    document.getDocumentType())
                    && "VERIFIED".equalsIgnoreCase(
                    document.getVerificationStatus())) {
                salarySlipVerified = true;
            }
        }

        return panVerified && aadhaarVerified && salarySlipVerified;
    }

    @Override
    public void deleteDocument(Integer documentId) {

        KycDocument document = findDocument(documentId);

        if (document.getFilePath() != null
                && !document.getFilePath().isBlank()) {

            try {
                Path storedPath = Paths.get(document.getFilePath())
                        .toAbsolutePath()
                        .normalize();

                if (storedPath.startsWith(uploadDirectory)) {
                    Files.deleteIfExists(storedPath);
                } else {
                    log.warn(
                            "Skipped deleting file outside upload directory: documentId={}",
                            documentId
                    );
                }

            } catch (IOException | InvalidPathException ex) {
                log.warn(
                        "Could not delete stored KYC file: documentId={}",
                        documentId,
                        ex
                );
            }
        }

        detailsRepository.delete(documentId);
        repository.delete(document);

        log.info("KYC document deleted: documentId={}", documentId);
    }

    private KycDocument findDocument(Integer documentId) {

        return repository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KYC document not found with id: " + documentId
                ));
    }

    private void validateCustomerId(Integer customerId) {

        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException(
                    "A valid customerId is required"
            );
        }
    }

    private String normalizeDocumentType(String documentType) {

        if (documentType == null || documentType.isBlank()) {
            throw new IllegalArgumentException(
                    "Document type is required"
            );
        }

        return documentType.trim().toUpperCase();
    }

    private void validateDocumentType(String documentType) {

        if (!REQUIRED_DOCUMENT_TYPES.contains(documentType)) {
            throw new IllegalArgumentException(
                    "Document type must be PAN, AADHAAR or SALARY_SLIP"
            );
        }
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a document file"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size must not exceed 10 MB"
            );
        }

        if (file.getContentType() == null
                || !ALLOWED_CONTENT_TYPES.contains(
                file.getContentType().toLowerCase().trim())) {
            throw new IllegalArgumentException(
                    "Only PDF, JPG/JPEG and PNG files are allowed"
            );
        }
    }

    private String getOriginalFileName(MultipartFile file) {

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            return "document";
        }

        try {
            return Paths.get(originalFileName)
                    .getFileName()
                    .toString();
        } catch (InvalidPathException ex) {
            throw new IllegalArgumentException(
                    "Invalid uploaded file name"
            );
        }
    }

    private String getFileExtension(String fileName) {

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex < 0) {
            return "";
        }

        return fileName.substring(dotIndex)
                .replaceAll("[^A-Za-z0-9.]", "");
    }

    private void validateFileSignature(
            MultipartFile file,
            String contentType) {

        try (var inputStream = file.getInputStream()) {

            byte[] header = inputStream.readNBytes(8);

            boolean valid = switch (contentType) {
                case "application/pdf" ->
                        header.length >= 4
                                && header[0] == '%'
                                && header[1] == 'P'
                                && header[2] == 'D'
                                && header[3] == 'F';

                case "image/jpeg" ->
                        header.length >= 3
                                && (header[0] & 0xFF) == 0xFF
                                && (header[1] & 0xFF) == 0xD8
                                && (header[2] & 0xFF) == 0xFF;

                case "image/png" ->
                        header.length >= 8
                                && (header[0] & 0xFF) == 0x89
                                && header[1] == 'P'
                                && header[2] == 'N'
                                && header[3] == 'G';

                default -> false;
            };

            if (!valid) {
                throw new IllegalArgumentException(
                        "File content does not match the declared file type"
                );
            }

        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "Could not read uploaded file",
                    ex
            );
        }
    }

    private String normalizeStatus(String status) {

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Verification status is required"
            );
        }

        return status.trim().toUpperCase();
    }

    private void validateVerificationStatus(String status) {

        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "Status must be PENDING, VERIFIED or REJECTED"
            );
        }
    }

    private void validateRejectionReason(
            String status,
            String rejectionReason) {

        if ("REJECTED".equals(status)
                && (rejectionReason == null
                || rejectionReason.isBlank())) {
            throw new IllegalArgumentException(
                    "A rejection reason is required when rejecting a document"
            );
        }
    }

    private String getContentTypeFromPath(String filePath) {

        if (filePath == null || filePath.isBlank()) {
            return "application/octet-stream";
        }

        String path = filePath.toLowerCase();

        if (path.endsWith(".pdf")) {
            return "application/pdf";
        }

        if (path.endsWith(".png")) {
            return "image/png";
        }

        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        return "application/octet-stream";
    }

    private KycDocumentResponse toResponse(KycDocument document) {

        KycDocumentResponse response = modelMapper.map(
                document,
                KycDocumentResponse.class
        );

        Integer documentId = document.getDocumentId();

        if (documentId == null) {
            return response;
        }

        if (document.getFilePath() != null
                && !document.getFilePath().isBlank()) {
            response.setFilePath(
                    "/api/kyc/documents/" + documentId + "/file"
            );
        }

        detailsRepository.find(documentId).ifPresent(details -> {
            response.setOriginalFileName(details.originalFileName());
            response.setContentType(details.contentType());
            response.setRejectionReason(details.rejectionReason());
            response.setReviewedAt(details.reviewedAt());
        });

        if (response.getOriginalFileName() == null
                && document.getFilePath() != null) {

            try {
                response.setOriginalFileName(
                        Paths.get(document.getFilePath())
                                .getFileName()
                                .toString()
                );
            } catch (InvalidPathException ex) {
                response.setOriginalFileName(
                        "document-" + documentId
                );
            }
        }

        if (response.getContentType() == null) {
            response.setContentType(
                    getContentTypeFromPath(document.getFilePath())
            );
        }

        return response;
    }
}
