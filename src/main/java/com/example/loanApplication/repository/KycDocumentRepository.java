package com.example.loanApplication.repository;

import com.example.loanApplication.entity.KycDocument;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KycDocumentRepository  extends JpaRepository<KycDocument, Integer> {


   /* @PersistenceContext
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
    }*/

    /*public List<KycDocument> findByCustomerId(Integer customerId) {
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
*/
  /*public void delete(KycDocument document) {
        KycDocument managed = entityManager.contains(document)
                ? document : entityManager.merge(document);
        entityManager.remove(managed);
    }*/

    List<KycDocument> findByCustomerId(Integer customerId);

    // 3. Find documents by Customer ID
  /*  @Query("""
            SELECT k
            FROM KycDocument k
            WHERE k.customerId = :customerId
            ORDER BY k.documentId
            """)
    List<KycDocument> findByCustomerId(
            @Param("customerId") Integer customerId
    );
*/
    // 4. Find documents by verification status (case-insensitive)
    @Query("""
            SELECT k
            FROM KycDocument k
            WHERE UPPER(k.verificationStatus) = UPPER(:status)
            ORDER BY k.documentId
            """)
    List<KycDocument> findByStatus(
            @Param("status") String status
    );

    // 5. Count documents by Customer ID and Document Type
    @Query("""
            SELECT COUNT(k)
            FROM KycDocument k
            WHERE k.customerId = :customerId
              AND UPPER(k.documentType) = UPPER(:type)
            """)
    long countByCustomerAndType(
            @Param("customerId") Integer customerId,
            @Param("type") String type
    );

    Long countByVerificationStatus(String verificationStatus);


}
