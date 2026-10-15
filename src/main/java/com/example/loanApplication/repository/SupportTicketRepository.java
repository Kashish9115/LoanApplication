package com.example.loanApplication.repository;

import com.example.loanApplication.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Repository contains only Spring Data query declarations. */
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Integer> {
    List<SupportTicket> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);
    List<SupportTicket> findByStatusIgnoreCaseOrderByCreatedAtAsc(String status);
    Page<SupportTicket> findByCustomerId(Integer customerId, Pageable pageable);
    Page<SupportTicket> findByStatusIgnoreCase(String status, Pageable pageable);
}
