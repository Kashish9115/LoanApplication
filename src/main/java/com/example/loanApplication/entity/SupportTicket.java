package com.example.loanApplication.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "SupportTickets")
@Data
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TicketId")
    private Integer ticketId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "LoanAccountId")
    private Integer loanAccountId;

    @Column(name = "Subject", length = 255)
    private String subject;

    @Column(name = "Description", length = 2000)
    private String description;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
