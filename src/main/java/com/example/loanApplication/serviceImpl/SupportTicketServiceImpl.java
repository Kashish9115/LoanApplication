package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.*;
import com.example.loanApplication.entity.SupportTicket;
import com.example.loanApplication.repository.SupportTicketRepository;
import com.example.loanApplication.service.SupportTicketService;
import com.example.loanApplication.exception.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {
    private static final Set<String> STATUSES =
            Set.of("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");

    private final SupportTicketRepository repository;
    private final ModelMapper modelMapper;

    public SupportTicketServiceImpl(SupportTicketRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public SupportTicketResponse raiseTicket(SupportTicketRequest request) {
        SupportTicket ticket = modelMapper.map(request, SupportTicket.class);
        ticket.setTicketId(null);
        LocalDateTime now = LocalDateTime.now();
        ticket.setStatus("OPEN");
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        return response(repository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getTicket(Integer ticketId) {
        return response(required(ticketId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getCustomerTickets(Integer customerId) {
        return repository.findByCustomerId(customerId).stream().map(this::response).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getTicketsByStatus(String status) {
        validateStatus(status);
        return repository.findByStatus(status).stream().map(this::response).toList();
    }

    @Override
    public SupportTicketResponse updateStatus(Integer ticketId, SupportTicketStatusRequest request) {
        validateStatus(request.getStatus());
        SupportTicket ticket = required(ticketId);
        String nextStatus = request.getStatus().trim().toUpperCase();
        validateTransition(ticket.getStatus(), nextStatus);
        ticket.setStatus(nextStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        return response(repository.save(ticket));
    }

    @Override
    public void deleteTicket(Integer ticketId) {
        repository.delete(required(ticketId));
    }

    private SupportTicket required(Integer id) {
        SupportTicket ticket = repository.findById(id);
        if (ticket == null) throw new ResourceNotFoundException("Support ticket not found with id: " + id);
        return ticket;
    }

    private void validateTransition(String current, String next) {
        if (current == null || current.equals(next)) return;
        boolean valid = ("OPEN".equals(current) && "IN_PROGRESS".equals(next))
                || ("IN_PROGRESS".equals(current) && "RESOLVED".equals(next))
                || ("RESOLVED".equals(current) && "CLOSED".equals(next));
        if (!valid) {
            throw new IllegalStateException("Invalid status transition: " + current + " -> " + next);
        }
    }

    private void validateStatus(String status) {
        if (status == null || !STATUSES.contains(status.trim().toUpperCase())) {
            throw new IllegalArgumentException(
                    "Status must be OPEN, IN_PROGRESS, RESOLVED or CLOSED");
        }
    }

    private SupportTicketResponse response(SupportTicket ticket) {
        return modelMapper.map(ticket, SupportTicketResponse.class);
    }
}
