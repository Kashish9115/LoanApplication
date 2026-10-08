package com.example.loanApplication.repository;

import com.example.loanApplication.entity.LoanDeal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanDealRepository
        extends JpaRepository<LoanDeal, Integer> {

    Optional<LoanDeal>
    findTopByCustomerIdOrderByDealIdDesc(Integer customerId);

    Page<LoanDeal>
    findByCustomerIdOrderByDealIdDesc(
            Integer customerId,
            Pageable pageable
    );

}