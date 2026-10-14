package com.example.loanApplication.repository;

import com.example.loanApplication.entity.CibilReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CibilReportRepository
        extends JpaRepository<CibilReport, Integer> {

    Optional<CibilReport> findTopByCustomerIdOrderByCheckDateDesc(
            Integer customerId
    );
}