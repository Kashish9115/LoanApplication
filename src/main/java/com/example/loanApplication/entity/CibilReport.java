package com.example.loanApplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CibilReports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CibilReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CibilReportId")
    private Integer cibilReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
   private Customer customer;



    @Column(name = "PanNo", length = 20)
    private String panNo;

    @Column(name = "CibilScore")
    private Integer cibilScore;

    @Column(name = "CheckDate")
    private LocalDateTime checkDate;

    @Column(name = "status", length = 20)
    private String status;
}
