package com.example.loanApplication.repository;

import com.example.loanApplication.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Repository contains only Spring Data query declarations; business logic belongs in services. */
public interface KycDocumentRepository extends JpaRepository<KycDocument, Integer> {
    List<KycDocument> findByCustomerIdOrderByDocumentIdAsc(Integer customerId);
    List<KycDocument> findByVerificationStatusIgnoreCaseOrderByCustomerIdAscDocumentIdAsc(String verificationStatus);
    long countByCustomerIdAndDocumentTypeIgnoreCase(Integer customerId, String documentType);
    List<KycDocument> findAllByOrderByCustomerIdAscDocumentIdAsc();
    Page<KycDocument> findAllByOrderByCustomerIdAscDocumentIdAsc(Pageable pageable);
    Page<KycDocument> findByCustomerId(Integer customerId, Pageable pageable);
    Page<KycDocument> findByVerificationStatusIgnoreCase(String verificationStatus, Pageable pageable);
}
