package com.example.loanApplication.repository;

import com.example.loanApplication.entity.SupportTicket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SupportTicketRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public SupportTicket save(SupportTicket ticket) {
        if (ticket.getTicketId() == null) {
            entityManager.persist(ticket);
            return ticket;
        }
        return entityManager.merge(ticket);
    }

    public SupportTicket findById(Integer id) {
        return entityManager.find(SupportTicket.class, id);
    }

    public List<SupportTicket> findByCustomerId(Integer customerId) {
        return entityManager.createQuery(
                "SELECT t FROM SupportTicket t WHERE t.customerId = :customerId ORDER BY t.createdAt DESC",
                SupportTicket.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }

    public List<SupportTicket> findByStatus(String status) {
        return entityManager.createQuery(
                "SELECT t FROM SupportTicket t WHERE UPPER(t.status) = :status ORDER BY t.createdAt ASC",
                SupportTicket.class)
                .setParameter("status", status.toUpperCase())
                .getResultList();
    }

    public void delete(SupportTicket ticket) {
        SupportTicket managed = entityManager.contains(ticket)
                ? ticket : entityManager.merge(ticket);
        entityManager.remove(managed);
    }
}
