package com.example.loanApplication.repository;

import com.example.loanApplication.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KycDocumentRepository extends JpaRepository<KycDocument, Integer> {

    List<KycDocument> findByCustomerId(Integer customerId);

    List<KycDocument> findByVerificationStatus(String verificationStatus);
}
