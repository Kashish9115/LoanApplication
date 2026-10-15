package com.example.loanApplication.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Maps only the columns present in the supplied kycdocuments table. */
@Getter
@Setter
@Entity
@Table(name = "kycdocuments")
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
    private String verificationStatus = "PENDING";
}
