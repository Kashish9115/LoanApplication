
package com.example.loanApplication.controller;

import com.example.loanApplication.dto.SupportTicketRequest;
import com.example.loanApplication.dto.SupportTicketResponse;
import com.example.loanApplication.dto.SupportTicketStatusRequest;
import com.example.loanApplication.service.SupportTicketService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://127.0.0.1:4200"
})
public class SupportTicketController {

    private final SupportTicketService service;

    public SupportTicketController(SupportTicketService service) {
        this.service = service;
    }

    @PostMapping("/tickets")
    public ResponseEntity<SupportTicketResponse> raise(
            @Valid @RequestBody SupportTicketRequest request) {

        log.info(
                "Support ticket creation requested for customerId={}",
                request.getCustomerId()
        );

        SupportTicketResponse response = service.raiseTicket(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/customer/{customerId}/tickets")
    public ResponseEntity<List<SupportTicketResponse>> customerTickets(
            @PathVariable Integer customerId) {

        return ResponseEntity.ok(
                service.getCustomerTickets(customerId)
        );
    }

    @GetMapping("/customer/{customerId}/tickets/paged")
    public ResponseEntity<Page<SupportTicketResponse>> customerTicketsPaged(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = createPageable(page, size, "createdAt");

        return ResponseEntity.ok(
                service.getCustomerTicketsPage(customerId, pageable)
        );
    }

    @GetMapping("/officer/tickets")
    public ResponseEntity<List<SupportTicketResponse>> ticketsByStatus(
            @RequestParam(defaultValue = "OPEN") String status) {

        return ResponseEntity.ok(
                service.getTicketsByStatus(status)
        );
    }

    @GetMapping("/officer/tickets/paged")
    public ResponseEntity<Page<SupportTicketResponse>> ticketsByStatusPaged(
            @RequestParam(defaultValue = "OPEN") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = createPageable(page, size, "createdAt");

        return ResponseEntity.ok(
                service.getTicketsByStatusPage(status, pageable)
        );
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<SupportTicketResponse> getTicket(
            @PathVariable Integer ticketId) {

        return ResponseEntity.ok(
                service.getTicket(ticketId)
        );
    }

    @PutMapping("/officer/tickets/{ticketId}/status")
    public ResponseEntity<SupportTicketResponse> updateStatus(
            @PathVariable Integer ticketId,
            @Valid @RequestBody SupportTicketStatusRequest request) {

        log.info(
                "Support ticket status update requested: ticketId={}, status={}",
                ticketId,
                request.getStatus()
        );

        SupportTicketResponse response =
                service.updateStatus(ticketId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tickets/{ticketId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer ticketId) {

        service.deleteTicket(ticketId);

        return ResponseEntity.noContent().build();
    }

    private Pageable createPageable(int page, int size, String sortBy) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, sortBy)
        );
    }
}