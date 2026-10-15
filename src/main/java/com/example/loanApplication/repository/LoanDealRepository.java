package com.example.loanApplication.repository;

import com.example.loanApplication.entity.LoanDeal;

import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.enumeration.LoanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
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

    //dashboard method
    Long countByDealStatus(LoanDealStatus dealStatus);

    //report methods
    // --- Without Date Filter ---

    Long countByLoanType(LoanType loanType);

    Long countByLoanTypeAndDealStatus(
            LoanType loanType,
            LoanDealStatus dealStatus
    );

// --- With Date Filter (PreferredDisbursementDate) ---

    Long countByLoanTypeAndPreferredDisbursementDateBetween(
            LoanType loanType,
            LocalDate fromDate,
            LocalDate toDate
    );

    Long countByLoanTypeAndDealStatusAndPreferredDisbursementDateBetween(
            LoanType loanType,
            LoanDealStatus dealStatus,
            LocalDate fromDate,
            LocalDate toDate
    );


    List<LoanDeal> findByCustomerIdOrderByDealIdDesc(Integer customerId);



}