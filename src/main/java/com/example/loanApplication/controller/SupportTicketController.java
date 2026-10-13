package com.example.loanApplication.controller;

import com.example.loanApplication.dto.*;
import com.example.loanApplication.service.SupportTicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/support")
public class SupportTicketController {
    private final SupportTicketService service;

    public SupportTicketController(SupportTicketService service) {
        this.service = service;
    }

    // CUSTOMER: raise a support ticket
    @PostMapping("/tickets")
    public ResponseEntity<SupportTicketResponse> raise(
            @Valid @RequestBody SupportTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.raiseTicket(request));
    }

    // CUSTOMER: view own tickets
    @GetMapping("/customer/{customerId}/tickets")
    public ResponseEntity<List<SupportTicketResponse>> customerTickets(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(service.getCustomerTickets(customerId));
    }

    // SUPPORT OFFICER: fetch tickets by status
    @GetMapping("/officer/tickets")
    public ResponseEntity<List<SupportTicketResponse>> ticketsByStatus(
            @RequestParam(defaultValue = "OPEN") String status) {
        return ResponseEntity.ok(service.getTicketsByStatus(status));
    }

    // SUPPORT OFFICER: view ticket
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<SupportTicketResponse> ticket(
            @PathVariable Integer ticketId) {
        return ResponseEntity.ok(service.getTicket(ticketId));
    }

    // SUPPORT OFFICER: update ticket status
    @PutMapping("/officer/tickets/{ticketId}/status")
    public ResponseEntity<SupportTicketResponse> updateStatus(
            @PathVariable Integer ticketId,
            @Valid @RequestBody SupportTicketStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(ticketId, request));
    }

    @DeleteMapping("/tickets/{ticketId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer ticketId) {
        service.deleteTicket(ticketId);
        return ResponseEntity.noContent().build();
    }
}
