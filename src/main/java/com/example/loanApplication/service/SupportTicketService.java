package com.example.loanApplication.service;

import com.example.loanApplication.dto.*;
import java.util.List;

public interface SupportTicketService {
    SupportTicketResponse raiseTicket(SupportTicketRequest request);
    SupportTicketResponse getTicket(Integer ticketId);
    List<SupportTicketResponse> getCustomerTickets(Integer customerId);
    List<SupportTicketResponse> getTicketsByStatus(String status);
    SupportTicketResponse updateStatus(Integer ticketId, SupportTicketStatusRequest request);
    void deleteTicket(Integer ticketId);
}
