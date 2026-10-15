package com.example.loanApplication.service;

import com.example.loanApplication.dto.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupportTicketService {
    SupportTicketResponse raiseTicket(SupportTicketRequest request);
    SupportTicketResponse getTicket(Integer ticketId);
    List<SupportTicketResponse> getCustomerTickets(Integer customerId);
    List<SupportTicketResponse> getTicketsByStatus(String status);
    Page<SupportTicketResponse> getCustomerTicketsPage(Integer customerId, Pageable pageable);
    Page<SupportTicketResponse> getTicketsByStatusPage(String status, Pageable pageable);
    SupportTicketResponse updateStatus(Integer ticketId, SupportTicketStatusRequest request);
    void deleteTicket(Integer ticketId);
}
