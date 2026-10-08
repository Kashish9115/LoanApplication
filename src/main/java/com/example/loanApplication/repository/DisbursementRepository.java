package com.example.loanApplication.repository;

import com.example.loanApplication.entity.Disbursement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisbursementRepository
        extends JpaRepository<Disbursement, Integer> {

    Optional<Disbursement>
    findTopByDealIdOrderByDisbursementIdDesc(
            Integer dealId
    );

    Page<Disbursement>
    findByDealIdOrderByDisbursementIdDesc(
            Integer dealId,
            Pageable pageable
    );
}