package com.example.loanApplication.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "KycDocuments")
@Data
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
}
