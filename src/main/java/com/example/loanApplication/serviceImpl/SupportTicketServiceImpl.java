
package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.SupportTicketRequest;
import com.example.loanApplication.dto.SupportTicketResponse;
import com.example.loanApplication.dto.SupportTicketStatusRequest;
import com.example.loanApplication.entity.SupportTicket;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.SupportTicketRepository;
import com.example.loanApplication.service.SupportTicketService;

import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {

    private static final Set<String> STATUSES =
            Set.of("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");

    private final SupportTicketRepository repository;
    private final ModelMapper modelMapper;

    public SupportTicketServiceImpl(
            SupportTicketRepository repository,
            ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public SupportTicketResponse raiseTicket(SupportTicketRequest request) {

        SupportTicket ticket = modelMapper.map(request, SupportTicket.class);

        LocalDateTime now = LocalDateTime.now();

        ticket.setStatus("OPEN");
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        SupportTicket savedTicket = repository.save(ticket);

        log.info(
                "Support ticket created: ticketId={}, customerId={}",
                savedTicket.getTicketId(),
                savedTicket.getCustomerId()
        );

        return modelMapper.map(savedTicket, SupportTicketResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getTicket(Integer ticketId) {
        return modelMapper.map(
                findTicket(ticketId),
                SupportTicketResponse.class
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getCustomerTickets(Integer customerId) {

        return repository
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(ticket -> modelMapper.map(
                        ticket,
                        SupportTicketResponse.class
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getTicketsByStatus(String status) {

        String normalizedStatus = validateStatus(status);

        return repository
                .findByStatusIgnoreCaseOrderByCreatedAtAsc(normalizedStatus)
                .stream()
                .map(ticket -> modelMapper.map(
                        ticket,
                        SupportTicketResponse.class
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getCustomerTicketsPage(
            Integer customerId,
            Pageable pageable) {

        return repository
                .findByCustomerId(customerId, pageable)
                .map(ticket -> modelMapper.map(
                        ticket,
                        SupportTicketResponse.class
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getTicketsByStatusPage(
            String status,
            Pageable pageable) {

        String normalizedStatus = validateStatus(status);

        return repository
                .findByStatusIgnoreCase(normalizedStatus, pageable)
                .map(ticket -> modelMapper.map(
                        ticket,
                        SupportTicketResponse.class
                ));
    }

    @Override
    public SupportTicketResponse updateStatus(
            Integer ticketId,
            SupportTicketStatusRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Ticket status request is required"
            );
        }

        String nextStatus = validateStatus(request.getStatus());
        SupportTicket ticket = findTicket(ticketId);

        String previousStatus = ticket.getStatus();

        validateTransition(previousStatus, nextStatus);

        ticket.setStatus(nextStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        SupportTicket updatedTicket = repository.save(ticket);

        log.info(
                "Support ticket status updated: ticketId={}, from={}, to={}",
                ticketId,
                previousStatus,
                nextStatus
        );

        return modelMapper.map(
                updatedTicket,
                SupportTicketResponse.class
        );
    }

    @Override
    public void deleteTicket(Integer ticketId) {

        SupportTicket ticket = findTicket(ticketId);

        repository.delete(ticket);

        log.info("Support ticket deleted: ticketId={}", ticketId);
    }

    private SupportTicket findTicket(Integer ticketId) {

        return repository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Support ticket not found with id: " + ticketId
                ));
    }

    private String validateStatus(String status) {

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket status is required"
            );
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException(
                    "Status must be OPEN, IN_PROGRESS, RESOLVED or CLOSED"
            );
        }

        return normalizedStatus;
    }

    private void validateTransition(String current, String next) {

        if (current == null || current.equalsIgnoreCase(next)) {
            return;
        }

        boolean validTransition =
                ("OPEN".equalsIgnoreCase(current)
                        && "IN_PROGRESS".equals(next))
                        || ("IN_PROGRESS".equalsIgnoreCase(current)
                        && "RESOLVED".equals(next))
                        || ("RESOLVED".equalsIgnoreCase(current)
                        && "CLOSED".equals(next));

        if (!validTransition) {
            throw new IllegalStateException(
                    "Invalid status transition: "
                            + current + " -> " + next
            );
        }
    }
}
