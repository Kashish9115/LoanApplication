package com.example.loanApplication.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "KycDocuments")
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DocumentId")
    private Integer documentId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "DocumentType", nullable = false, length = 100)
    private String documentType;

    @Column(name = "FilePath", length = 1000)
    private String filePath;

    @Column(name = "VerificationStatus", length = 50)
    private String verificationStatus;

    public KycDocument() {
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
}
