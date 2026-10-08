package com.example.loanApplication.repository;

import com.example.loanApplication.entity.KycDocument;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class KycDocumentRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public KycDocument save(KycDocument document) {
        if (document.getDocumentId() == null) {
            entityManager.persist(document);
            return document;
        }
        return entityManager.merge(document);
    }

    public KycDocument findById(Integer id) {
        return entityManager.find(KycDocument.class, id);
    }

    public List<KycDocument> findByCustomerId(Integer customerId) {
        return entityManager.createQuery(
                "SELECT k FROM KycDocument k WHERE k.customerId = :customerId ORDER BY k.documentId",
                KycDocument.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }

    public List<KycDocument> findByStatus(String status) {
        return entityManager.createQuery(
                "SELECT k FROM KycDocument k WHERE UPPER(k.verificationStatus) = :status ORDER BY k.documentId",
                KycDocument.class)
                .setParameter("status", status.toUpperCase())
                .getResultList();
    }

    public long countByCustomerAndType(Integer customerId, String type) {
        return entityManager.createQuery(
                "SELECT COUNT(k) FROM KycDocument k WHERE k.customerId = :customerId AND UPPER(k.documentType) = :type",
                Long.class)
                .setParameter("customerId", customerId)
                .setParameter("type", type.toUpperCase())
                .getSingleResult();
    }

    public void delete(KycDocument document) {
        KycDocument managed = entityManager.contains(document)
                ? document : entityManager.merge(document);
        entityManager.remove(managed);
    }
}
